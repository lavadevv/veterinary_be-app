package ext.vnua.veterinary_beapp.modules.product.servies.impl;


import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.material.repository.LocationRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialBatchRepository;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductBatchDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.CompleteBatchRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.IssueBatchRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.SimulateConsumptionRequest;
import ext.vnua.veterinary_beapp.modules.product.enums.ProductBatchStatus;
import ext.vnua.veterinary_beapp.modules.product.mapper.ProductBatchMapper;
import ext.vnua.veterinary_beapp.modules.product.model.*;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductBatchConsumptionRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductBatchRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductFormulaRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.custom.CustomProductBatchQuery;
import ext.vnua.veterinary_beapp.modules.product.servies.ProductBatchService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductBatchServiceImpl implements ProductBatchService {
    private final ProductBatchRepository batchRepo;
    private final ProductRepository productRepo;
    private final ProductFormulaRepository formulaRepo;
    private final ProductBatchMapper batchMapper;
    private final MaterialBatchRepository materialBatchRepo; // bạn đã có trong module material
    private final LocationRepository locationRepo;
    private final ProductBatchConsumptionRepository consumptionRepository;

    @Override
    public Page<ProductBatch> getAllBatches(CustomProductBatchQuery.ProductBatchFilterParam param, PageRequest pageRequest) {
        Specification<ProductBatch> spec = CustomProductBatchQuery.getFilter(param);
        return batchRepo.findAll(spec, pageRequest);
    }

    @Override
    public ProductBatchDto getById(Long id) {
        var b = batchRepo.findById(id).orElseThrow(() -> new DataExistException("Batch không tồn tại"));
        return batchMapper.toDto(b);
    }

    @Override
    public ProductBatchDto getByBatchNumber(String batchNumber) {
        var b = batchRepo.findByBatchNumber(batchNumber).orElseThrow(() -> new DataExistException("Mã batch không tồn tại"));
        return batchMapper.toDto(b);
    }

    @Override
    public Map<Long, BigDecimal> calculateMaterialNeeds(Long productId, BigDecimal plannedQty, Long formulaId) {
        ProductFormula formula = resolveFormula(productId, formulaId);
        // Định mức đang tính theo "số lượng thành phẩm" (bạn đã xác nhận)
        Map<Long, BigDecimal> needs = new HashMap<>();
        for (ProductFormulaItem it : formula.getFormulaItems()) {
            BigDecimal need = it.getQuantity().multiply(plannedQty); // đơn giản: định mức * plannedQty
            needs.merge(it.getMaterial().getId(), need, BigDecimal::add);
        }
        return needs;
    }

    private ProductFormula resolveFormula(Long productId, Long formulaId) {
        if (formulaId != null) {
            return formulaRepo.findById(formulaId)
                    .orElseThrow(() -> new DataExistException("Công thức không tồn tại"));
        }
        return formulaRepo.findFirstActiveByProductIdWithProduct(productId)
                .orElseThrow(() -> new DataExistException("Sản phẩm chưa có công thức active"));
    }


    @Override
    public ConsumptionPlan simulateConsumption(SimulateConsumptionRequest request) {
        Map<Long, BigDecimal> needs = calculateMaterialNeeds(request.getProductId(), request.getPlannedQuantity(), request.getFormulaId());
        List<MaterialPick> picks = new ArrayList<>();
        List<Shortage> shortages = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> e : needs.entrySet()) {
            Long materialId = e.getKey();
            BigDecimal need = e.getValue();
            // FIFO: giả sử repo có hàm lấy theo hạn dùng, ngày nhập, ...
            List<MaterialBatch> fifo = materialBatchRepo.findAvailableByMaterialFifo(materialId);
            BigDecimal remain = need;

            for (MaterialBatch mb : fifo) {
                if (remain.signum() <= 0) break;
                BigDecimal take = mb.getAvailableQuantity().min(remain);
                if (take.signum() > 0) {
                    picks.add(new MaterialPick(mb.getId(), take));
                    remain = remain.subtract(take);
                }
            }
            if (remain.signum() > 0) {
                shortages.add(new Shortage(materialId, remain));
            }
        }
        return new ConsumptionPlan(picks, shortages);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "ProductBatch", description = "Phát hành lệnh sản xuất (issue)")
    public ProductBatchDto issueBatch(IssueBatchRequest req) {
        Product product = productRepo.findById(req.getProductId()).orElseThrow(() -> new DataExistException("Sản phẩm không tồn tại"));
        ProductFormula formula = resolveFormula(req.getProductId(), req.getFormulaId());

        if (req.getPlannedQuantity().signum() <= 0) throw new MyCustomException("Số lượng kế hoạch phải > 0");

        // 1) Nếu người dùng không cung cấp picks → mô phỏng FIFO
        ConsumptionPlan plan = (req.getPicks() == null || req.getPicks().isEmpty())
                ? simulateConsumption(toSimulateReq(req))
                : toConsumptionPlan(req);

        if (!plan.shortages().isEmpty()) {
            throw new MyCustomException("Thiếu NVL cho lệnh sản xuất " + plan.shortages());
        }

        // 2) Reserve NVL (khóa số lượng, không cho âm kho)
        reserveMaterials(plan.picks());

        // 3) Tạo batch + gen batch number: PROD-{productCode}-{yyyyMMdd}-{seq3}
        String batchNumber = generateBatchNumber(product.getProductCode(), req.getManufacturingDate());

        ProductBatch batch = new ProductBatch();
        batch.setProduct(product);
        batch.setFormula(formula);
        batch.setBatchNumber(batchNumber);
        batch.setManufacturingDate(req.getManufacturingDate());
        batch.setExpiryDate(calcExpiry(product, req.getManufacturingDate()));
        batch.setPlannedQuantity(req.getPlannedQuantity());
        batch.setRejectedQuantity(BigDecimal.ZERO);
        batch.setCurrentStock(BigDecimal.ZERO);
        batch.setStatus(ProductBatchStatus.IN_PROGRESS);

        return batchMapper.toDto(batchRepo.saveAndFlush(batch));
    }

    private SimulateConsumptionRequest toSimulateReq(IssueBatchRequest req) {
        SimulateConsumptionRequest s = new SimulateConsumptionRequest();
        s.setProductId(req.getProductId());
        s.setPlannedQuantity(req.getPlannedQuantity());
        s.setFormulaId(req.getFormulaId());
        return s;
    }

    private ConsumptionPlan toConsumptionPlan(IssueBatchRequest req) {
        // Nếu allowAlternativeBatches = true, coi picks do user gửi là ưu tiên
        List<MaterialPick> picks = req.getPicks().stream()
                .map(p -> new MaterialPick(p.getMaterialBatchId(), p.getQuantity()))
                .collect(Collectors.toList());
        // Vẫn check thiếu tổng thể (phòng user gửi thiếu)

        Map<Long, BigDecimal> needs = calculateMaterialNeeds(req.getProductId(), req.getPlannedQuantity(), req.getFormulaId());
        // TODO: load từng materialBatch để cộng gộp theo materialId và so với needs → shortages
        Map<Long, BigDecimal> pickedTotals = new HashMap<>();
        for (MaterialPick p : picks) {
            MaterialBatch mb = materialBatchRepo.findById(p.materialBatchId())
                    .orElseThrow(() -> new DataExistException("NVL không tồn tại: " + p.materialBatchId()));
            pickedTotals.merge(mb.getMaterial().getId(), p.quantity(), BigDecimal::add);
        }

        List<Shortage> shortages = new ArrayList<>();
        for (var e : needs.entrySet()) {
            BigDecimal provided = pickedTotals.getOrDefault(e.getKey(), BigDecimal.ZERO);
            if (provided.compareTo(e.getValue()) < 0) {
                shortages.add(new Shortage(e.getKey(), e.getValue().subtract(provided)));
            }
        }
        return new ConsumptionPlan(picks, shortages);
    }

    private void reserveMaterials(List<MaterialPick> picks) {
        // TODO: gọi repo NVL trừ "available" và tăng "reserved" (nếu bạn có 2 field)
        // hoặc trừ thẳng availableQuantity, validate không âm
        // Nếu âm → throw MyCustomException
        for (MaterialPick pick : picks) {
            MaterialBatch mb = materialBatchRepo.findById(pick.materialBatchId())
                    .orElseThrow(() -> new DataExistException("NVL không tồn tại: " + pick.materialBatchId()));

            if (mb.getAvailableQuantity().compareTo(pick.quantity()) < 0) {
                throw new MyCustomException("Không đủ NVL trong lô " + mb.getBatchNumber());
            }
            mb.setAvailableQuantity(mb.getAvailableQuantity().subtract(pick.quantity()));
            materialBatchRepo.saveAndFlush(mb);
        }
    }

    private String generateBatchNumber(String productCode, LocalDate mfgDate) {
        String ymd = mfgDate.toString().replaceAll("-", "");
        int seq = nextSequenceForDay(productCode, mfgDate); // TODO: implement (đếm trong ngày)
        return String.format("PROD-%s-%s-%03d", productCode, ymd, seq);
    }

    private int nextSequenceForDay(String productCode, LocalDate mfgDate) {
//         TODO: đếm batchNumber bắt đầu "PROD-{code}-{yyyymmdd}-", +1
        String prefix = String.format("PROD-%s-%s", productCode, mfgDate.toString().replaceAll("-", ""));
        long count = batchRepo.count((root, query, cb) ->
                cb.like(root.get("batchNumber"), prefix + "%"));
        return (int) count + 1;
    }

    private LocalDate calcExpiry(Product product, LocalDate mfgDate) {
        Integer months = product.getShelfLifeMonths();
        return (months == null || months <= 0) ? mfgDate : mfgDate.plusMonths(months);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductBatch", description = "Hoàn thành lô sản xuất")
    public ProductBatchDto completeBatch(CompleteBatchRequest req) {
        ProductBatch batch = batchRepo.findById(req.getBatchId()).orElseThrow(() -> new DataExistException("Batch không tồn tại"));

        // 1) Trừ NVL đã reserve (hoặc deduct theo consumption record)
        deductMaterialsForBatch(batch.getId());

        // 2) Nhập kho thành phẩm
        if (req.getActualQuantity().signum() < 0) throw new MyCustomException("Số lượng thực tế không hợp lệ");
        BigDecimal actual = req.getActualQuantity();
        batch.setActualQuantity(actual);
        batch.setCurrentStock(actual); // tùy mô hình kho TP (ở đây đơn giản set = actual)

        // 3) Tính hiệu suất
        BigDecimal yield = actual.multiply(BigDecimal.valueOf(100))
                .divide(batch.getPlannedQuantity(), 2, java.math.RoundingMode.HALF_UP);
        batch.setYieldPercentage(yield);

        // 4) Ghi nhận QC/file/location nếu có
        batch.setRejectedQuantity(Objects.requireNonNullElse(req.getRejectedQuantity(), BigDecimal.ZERO));
        batch.setQcCertificatePath(req.getQcCertificatePath());

        Location location = locationRepo.findById(req.getLocationId())
                .orElseThrow(() -> new DataExistException("Vị trí nhập kho không tồn tại"));
        batch.setLocation(location);
        batch.setNotes(req.getNotes());
        batch.setStatus(ProductBatchStatus.STORED);

        return batchMapper.toDto(batchRepo.saveAndFlush(batch));
    }

    private void deductMaterialsForBatch(Long batchId) {
        // TODO: thực hiện trừ NVL theo record reserve/consumption của batch
        // đảm bảo không âm, nếu âm → throw MyCustomException
        List<ProductBatchConsumption> consumptions = consumptionRepository.findByProductBatchId(batchId);
        for (ProductBatchConsumption c : consumptions) {
            MaterialBatch mb = c.getMaterialBatch();
            BigDecimal newQty = mb.getCurrentQuantity().subtract(c.getActualQuantity());
            if (newQty.signum() < 0) {
                throw new MyCustomException("Âm kho NVL cho batch " + mb.getBatchNumber());
            }
            mb.setCurrentQuantity(newQty);
            materialBatchRepo.saveAndFlush(mb);
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductBatch", description = "Đóng lô thành phẩm")
    public void closeBatch(Long batchId) {
        ProductBatch b = batchRepo.findById(batchId).orElseThrow(() -> new DataExistException("Batch không tồn tại"));
        b.setStatus(ProductBatchStatus.CLOSED);
        batchRepo.saveAndFlush(b);
    }

    @Override
    public void adjustBatchCurrentStock(Long batchId, BigDecimal delta) {
        ProductBatch b = batchRepo.findById(batchId).orElseThrow(() -> new DataExistException("Batch không tồn tại"));
        BigDecimal newStock = (b.getCurrentStock() == null ? BigDecimal.ZERO : b.getCurrentStock()).add(delta);
        if (newStock.signum() < 0) throw new MyCustomException("Không được âm tồn kho lô");
        b.setCurrentStock(newStock);
        batchRepo.saveAndFlush(b);
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "ProductBatch", description = "Xóa danh sách lô TP")
    public List<ProductBatchDto> deleteBatches(List<Long> ids) {
        List<ProductBatchDto> rs = new ArrayList<>();
        for (Long id : ids) {
            ProductBatch b = batchRepo.findById(id).orElseThrow(() -> new MyCustomException("Batch không tồn tại: " + id));
            rs.add(batchMapper.toDto(b));
            batchRepo.delete(b);
        }
        return rs;
    }
}