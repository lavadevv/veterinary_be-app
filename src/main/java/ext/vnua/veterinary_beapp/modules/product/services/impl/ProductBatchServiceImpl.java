package ext.vnua.veterinary_beapp.modules.product.services.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.material.repository.LocationRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialBatchRepository;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductBatchDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.*;
import ext.vnua.veterinary_beapp.modules.product.dto.response.productBatch.CalcBatchRes;
import ext.vnua.veterinary_beapp.modules.product.enums.ProductBatchStatus;
import ext.vnua.veterinary_beapp.modules.product.mapper.ProductBatchMapper;
import ext.vnua.veterinary_beapp.modules.product.model.*;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductBatchConsumptionRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductBatchRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductFormulaRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.custom.CustomProductBatchQuery;
import ext.vnua.veterinary_beapp.modules.product.services.ProductBatchService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final MaterialBatchRepository materialBatchRepo;
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

    /** Chuẩn hoá nhu cầu NVL về GRAM cho một lệnh theo công thức */
    @Override
    public Map<Long, BigDecimal> calculateMaterialNeeds(Long productId, BigDecimal plannedQtyKg, Long formulaId) {
        ProductFormula formula = resolveFormula(productId, formulaId);

        // Tính tổng % (cho công thức dạng %)
        BigDecimal sumPct = formula.getFormulaItems().stream()
                .map(ProductFormulaItem::getPercentage)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, BigDecimal> needsG = new HashMap<>();

        for (ProductFormulaItem it : formula.getFormulaItems()) {
            Long materialId = it.getMaterial().getId();
            BigDecimal needG;

            if (it.getQuantity() != null && it.getUnit() != null && !it.getUnit().isBlank()) {
                String u = it.getUnit().trim().toLowerCase();
                if (u.equals("g")) {
                    needG = it.getQuantity().multiply(plannedQtyKg); // g/kg * kg = g
                } else if (u.equals("kg")) {
                    needG = it.getQuantity().multiply(plannedQtyKg).multiply(BigDecimal.valueOf(1000));
                } else {
                    throw new DataExistException("Chưa hỗ trợ đơn vị định mức: " + u);
                }
            } else {
                // Theo % nhưng không ràng buộc 100; dùng tỉ lệ percent / sumPct
                BigDecimal pct = it.getPercentage();
                if (pct == null || pct.signum() <= 0) {
                    // Nếu không có % hoặc <=0 thì coi như 0 (bỏ qua)
                    continue;
                }
                needG = qtyG(plannedQtyKg, pct, sumPct);
            }

            needsG.merge(materialId, needG, BigDecimal::add);
        }
        return needsG;

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

            // FIFO (ưu tiên gần hết hạn trước), đã có availableQuantity
            List<MaterialBatch> fifo = materialBatchRepo.findAvailableByMaterialFifoUsable(
                    materialId,
                    ext.vnua.veterinary_beapp.modules.material.enums.TestStatus.DAT,
                    ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus.SAN_SANG_SU_DUNG,
                    LocalDate.now()
            );

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
        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new DataExistException("Sản phẩm không tồn tại"));
        ProductFormula formula = resolveFormula(req.getProductId(), req.getFormulaId());

        if (req.getPlannedQuantity() == null || req.getPlannedQuantity().signum() <= 0) {
            throw new MyCustomException("Số lượng kế hoạch phải > 0");
        }

        // 1) Tạo batch trước để có batchId
        LocalDate mfg = req.getManufacturingDate() != null ? req.getManufacturingDate() : LocalDate.now();
        String batchNumber = generateBatchNumber(product, mfg);

        ProductBatch batch = new ProductBatch();
        batch.setProduct(product);
        batch.setFormula(formula);
        batch.setBatchNumber(batchNumber);
        batch.setManufacturingDate(mfg);
        batch.setExpiryDate(calcExpiry(product, mfg));
        batch.setPlannedQuantity(req.getPlannedQuantity());
        batch.setRejectedQuantity(BigDecimal.ZERO);
        batch.setCurrentStock(BigDecimal.ZERO);
        batch.setStatus(ProductBatchStatus.IN_PROGRESS);
        batch = batchRepo.saveAndFlush(batch);

        // 2) Lập kế hoạch tiêu hao (nếu client không gửi picks ➜ mô phỏng FIFO)
        ConsumptionPlan plan = (req.getPicks() == null || req.getPicks().isEmpty())
                ? simulateConsumption(toSimulateReq(req))
                : toConsumptionPlan(req);

        if (!plan.shortages().isEmpty()) {
            throw new MyCustomException("Thiếu NVL cho lệnh sản xuất " + plan.shortages());
        }

        // 3) Reserve NVL + ghi consumption planned theo từng MaterialBatch
        reserveMaterials(batch.getId(), plan.picks());

        return batchMapper.toDto(batch);
    }

    private SimulateConsumptionRequest toSimulateReq(IssueBatchRequest req) {
        SimulateConsumptionRequest s = new SimulateConsumptionRequest();
        s.setProductId(req.getProductId());
        s.setPlannedQuantity(req.getPlannedQuantity());
        s.setFormulaId(req.getFormulaId());
        return s;
    }

    private ConsumptionPlan toConsumptionPlan(IssueBatchRequest req) {
        List<MaterialPick> picks = req.getPicks().stream()
                .map(p -> new MaterialPick(p.getMaterialBatchId(), p.getQuantity()))
                .collect(Collectors.toList());

        // Kiểm tra tổng cung cấp có đáp ứng nhu cầu
        Map<Long, BigDecimal> needs = calculateMaterialNeeds(req.getProductId(), req.getPlannedQuantity(), req.getFormulaId());

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

    /** Reserve + ghi consumption planned */
    private void reserveMaterials(Long batchId, List<MaterialPick> picks) {
        if (picks == null || picks.isEmpty()) return;

        List<ProductBatchConsumption> toSave = new ArrayList<>();

        for (MaterialPick pick : picks) {
            MaterialBatch mb = materialBatchRepo.findById(pick.materialBatchId())
                    .orElseThrow(() -> new DataExistException("NVL không tồn tại: " + pick.materialBatchId()));

            BigDecimal need = pick.quantity();
            if (need == null || need.signum() <= 0) continue;

            // Không cho âm available
            if (mb.getAvailableQuantity().compareTo(need) < 0) {
                throw new MyCustomException("Không đủ NVL trong lô " + mb.getBatchNumber());
            }

            // Cập nhật tồn kho đặt chỗ
            BigDecimal newAvail = mb.getAvailableQuantity().subtract(need);
            BigDecimal newReserved = (mb.getReservedQuantity() == null ? BigDecimal.ZERO : mb.getReservedQuantity()).add(need);
            mb.setAvailableQuantity(newAvail);
            mb.setReservedQuantity(newReserved);
            materialBatchRepo.saveAndFlush(mb);

            // Ghi consumption planned theo từng lô NVL
            ProductBatchConsumption c = new ProductBatchConsumption();
            c.setProductBatch(batchRepo.getReferenceById(batchId));
            c.setMaterialBatch(mb);
            c.setPlannedQuantity(need);
            c.setActualQuantity(BigDecimal.ZERO);
            toSave.add(c);
        }

        if (!toSave.isEmpty()) {
            consumptionRepository.saveAll(toSave);
        }
    }

    private String generateBatchNumber(Product product, LocalDate mfgDate) {
        String prefix = product.getProductCode() + "-" + mfgDate; // PRD001-2025-10-08
        List<String> existed = batchRepo.findBatchNumbersByProductAndDate(product.getId(), mfgDate);

        int nextSeq = 1;
        for (String bn : existed) {
            if (bn == null) continue;
            if (bn.startsWith(prefix + "-")) {
                String tail = bn.substring((prefix + "-").length()).trim();
                try {
                    int n = Integer.parseInt(tail);
                    if (n >= nextSeq) nextSeq = n + 1;
                } catch (NumberFormatException ignore) {}
            } else if (bn.equals(prefix)) {
                if (nextSeq <= 1) nextSeq = 2;
            }
        }
        String candidate = prefix + "-" + String.format("%03d", nextSeq);
        while (batchRepo.existsByBatchNumber(candidate)) {
            nextSeq++;
            candidate = prefix + "-" + String.format("%03d", nextSeq);
        }
        return candidate;
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

        // 1) Trừ NVL theo consumption (actual = planned nếu FE không cung cấp actual riêng)
        deductMaterialsForBatch(batch.getId());

        // 2) Nhập kho thành phẩm
        if (req.getActualQuantity().signum() < 0) throw new MyCustomException("Số lượng thực tế không hợp lệ");
        BigDecimal actual = req.getActualQuantity();
        batch.setActualQuantity(actual);
        batch.setCurrentStock(actual);

        // 3) Tính hiệu suất
        BigDecimal yield = actual.multiply(BigDecimal.valueOf(100))
                .divide(batch.getPlannedQuantity(), 2, RoundingMode.HALF_UP);
        batch.setYieldPercentage(yield);

        // 4) Ghi thông tin QC & vị trí
        batch.setRejectedQuantity(Objects.requireNonNullElse(req.getRejectedQuantity(), BigDecimal.ZERO));
        batch.setQcCertificatePath(req.getQcCertificatePath());

        Location location = locationRepo.findById(req.getLocationId())
                .orElseThrow(() -> new DataExistException("Vị trí nhập kho không tồn tại"));
        batch.setLocation(location);
        batch.setNotes(req.getNotes());
        batch.setStatus(ProductBatchStatus.STORED);

        return batchMapper.toDto(batchRepo.saveAndFlush(batch));
    }

    /** Khấu trừ từ reserved → current (và cập nhật available) trên từng lô NVL */
    private void deductMaterialsForBatch(Long batchId) {
        List<ProductBatchConsumption> consumptions = consumptionRepository.findByProductBatchId(batchId);

        for (ProductBatchConsumption c : consumptions) {
            MaterialBatch mb = c.getMaterialBatch();

            // Nếu actual chưa set, mặc định = planned
            BigDecimal actual = (c.getActualQuantity() == null || c.getActualQuantity().signum() == 0)
                    ? c.getPlannedQuantity() : c.getActualQuantity();

            // Không cho âm reserved
            BigDecimal reserved = (mb.getReservedQuantity() == null ? BigDecimal.ZERO : mb.getReservedQuantity());
            if (reserved.compareTo(actual) < 0) {
                throw new MyCustomException("Reserved không đủ để trừ cho lô NVL " + mb.getBatchNumber());
            }

            BigDecimal newReserved = reserved.subtract(actual);
            BigDecimal newCurrent = mb.getCurrentQuantity().subtract(actual);
            if (newCurrent.signum() < 0) {
                throw new MyCustomException("Âm kho NVL cho lô " + mb.getBatchNumber());
            }

            mb.setReservedQuantity(newReserved);
            // available = max(current - reserved, 0)
            BigDecimal recomputedAvailable = newCurrent.subtract(newReserved);
            if (recomputedAvailable.signum() < 0) recomputedAvailable = BigDecimal.ZERO;
            mb.setAvailableQuantity(recomputedAvailable);
            mb.setCurrentQuantity(newCurrent);

            Location loc = mb.getLocation();
            updateLocationCapacitySafe(loc, -actual.doubleValue()); // trừ sức chứa theo lượng NVL đã tiêu hao


            materialBatchRepo.saveAndFlush(mb);

            // Lưu lại actual (nếu chưa)
            if (c.getActualQuantity() == null || c.getActualQuantity().signum() == 0) {
                c.setActualQuantity(actual);
                consumptionRepository.save(c);
            }
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

    @Override
    public CalcBatchRes calc(CalcBatchReq req) {
        ProductFormula f = formulaRepo.findById(req.formulaId())
                .orElseThrow(() -> new DataExistException("Không tìm thấy công thức"));

        List<ProductFormulaItem> items = new ArrayList<>(f.getFormulaItems());
        items.sort(Comparator.comparing(ProductFormulaItem::getId));

        // Tổng % thực tế (có thể != 100)
        BigDecimal sumPct = items.stream()
                .map(ProductFormulaItem::getPercentage)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<CalcBatchRes.Line> lines = new ArrayList<>();
        int order = 1;

        for (ProductFormulaItem it : items) {
            var m = it.getMaterial();

            BigDecimal qg;
            if (it.getQuantity() != null && it.getUnit() != null && !it.getUnit().isBlank()) {
                // Tôn trọng định mức tuyệt đối nếu có
                String u = it.getUnit().trim().toLowerCase();
                if (u.equals("g")) {
                    qg = it.getQuantity().multiply(req.batchSizeKg());
                } else if (u.equals("kg")) {
                    qg = it.getQuantity().multiply(req.batchSizeKg()).multiply(BigDecimal.valueOf(1000));
                } else {
                    throw new DataExistException("Chưa hỗ trợ đơn vị định mức: " + u);
                }
            } else {
                BigDecimal pct = it.getPercentage();
                if (pct == null || pct.signum() <= 0) continue; // bỏ dòng % <= 0
                qg = qtyG(req.batchSizeKg(), pct, sumPct);
            }

            qg = qg.setScale(3, RoundingMode.HALF_UP);
            BigDecimal pG = pricePerG(m);
            BigDecimal amt = qg.multiply(pG).setScale(0, RoundingMode.HALF_UP);
            totalAmount = totalAmount.add(amt);

            lines.add(new CalcBatchRes.Line(
                    order++,
                    m.getId(),
                    m.getMaterialCode(),
                    m.getMaterialName(),
                    "g",
                    it.getPercentage(), // vẫn trả về % input để hiển thị
                    qg,
                    pG,
                    amt
            ));
        }

        return new CalcBatchRes(
                f.getProduct().getId(),
                f.getId(),
                req.batchSizeKg(),
                lines,
                new CalcBatchRes.Totals(req.batchSizeKg(), totalAmount)
        );

    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "ProductBatch", description = "Tạo lô sản phẩm (nháp) từ công thức")
    public ProductBatchDto createDraft(CreateBatchReq req) {
        // Tính lại để đảm bảo số liệu
        LocalDate mfg = LocalDate.now();
        this.calc(new CalcBatchReq(req.formulaId(), req.batchSizeKg()));
        ProductFormula f = formulaRepo.getReferenceById(req.formulaId());

        ProductBatch b = new ProductBatch();
        b.setProduct(f.getProduct());
        b.setFormula(f);
        b.setBatchNumber(generateBatchNumber(f.getProduct(), mfg));
        b.setManufacturingDate(mfg);
        b.setExpiryDate(calcExpiry(f.getProduct(), mfg));
        b.setPlannedQuantity(req.batchSizeKg()); // kg
        b.setActualQuantity(null);
        b.setYieldPercentage(null);
        b.setCurrentStock(BigDecimal.ZERO);
        b.setStatus(ProductBatchStatus.DRAFT);
        b.setNotes(req.note());

        batchRepo.saveAndFlush(b);
        return batchMapper.toDto(b);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductBatch", description = "Hủy lệnh sản xuất (unissue)")
    public void cancelBatch(CancelBatchRequest request) {
        ProductBatch b = batchRepo.findById(request.getBatchId())
                .orElseThrow(() -> new DataExistException("Batch không tồn tại"));

        // Idempotent: nếu đã hủy thì thoát sớm
        if (b.getStatus() == ProductBatchStatus.CANCELED) {
            return;
        }

        if (b.getStatus() != ProductBatchStatus.IN_PROGRESS && b.getStatus() != ProductBatchStatus.DRAFT) {
            throw new MyCustomException("Chỉ có thể hủy lệnh ở trạng thái DRAFT/IN_PROGRESS");
        }
        if (b.getActualQuantity() != null && b.getActualQuantity().signum() > 0) {
            throw new MyCustomException("Lô đã ghi nhận thành phẩm, không thể hủy. Hãy dùng quy trình khác (return/adjust).");
        }

        List<ProductBatchConsumption> lines = consumptionRepository.findByProductBatchId(b.getId());

        boolean anyActualUsed = lines.stream()
                .anyMatch(l -> l.getActualQuantity() != null && l.getActualQuantity().signum() > 0);
        if (anyActualUsed) {
            throw new MyCustomException("Đã có tiêu hao thực tế ở một số NVL, không thể hủy lệnh.");
        }

        // Xả reserve nếu còn
        for (ProductBatchConsumption c : lines) {
            MaterialBatch mb = c.getMaterialBatch();
            BigDecimal planned = nz(c.getPlannedQuantity());

            if (mb.getReservedQuantity() == null) mb.setReservedQuantity(BigDecimal.ZERO);
            if (mb.getAvailableQuantity() == null) mb.setAvailableQuantity(BigDecimal.ZERO);

            mb.setReservedQuantity(mb.getReservedQuantity().subtract(planned));
            if (mb.getReservedQuantity().signum() < 0) mb.setReservedQuantity(BigDecimal.ZERO);

            mb.setAvailableQuantity(mb.getAvailableQuantity().add(planned));
            materialBatchRepo.saveAndFlush(mb);
        }

        if (!lines.isEmpty()) {
            consumptionRepository.deleteAllInBatch(lines);
        }

        b.setStatus(ProductBatchStatus.CANCELED);
        String note = (request.getReason() == null || request.getReason().isBlank())
                ? "Hủy lệnh sản xuất & xả NVL dự trữ."
                : "Hủy lệnh sản xuất: " + request.getReason();
        b.setNotes((b.getNotes() == null ? "" : b.getNotes() + "\n") + note);
        batchRepo.saveAndFlush(b);
    }


    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }


    private static BigDecimal pricePerG(Material m) {
        if (m.getFixedPrice() == null) throw new DataExistException("Nguyên liệu " + m.getMaterialName() + " chưa có đơn giá");
        String uom = (m.getUnitOfMeasure() == null ? "g" : m.getUnitOfMeasure()).trim().toLowerCase();
        BigDecimal price = m.getFixedPrice();
        return switch (uom) {
            case "kg" -> price.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
            case "g"  -> price;
            default   -> throw new DataExistException("Chưa hỗ trợ định giá theo đơn vị: " + uom);
        };
    }

//    private static BigDecimal qtyG(BigDecimal batchSizeKg, BigDecimal percent) {
//        // percent là % thật: 0.3, 20.0,...
//        return batchSizeKg.multiply(percent)
//                .divide(BigDecimal.valueOf(100), 9, RoundingMode.HALF_UP)
//                .multiply(BigDecimal.valueOf(1000)); // kg -> g
//    }

    private static BigDecimal qtyG(BigDecimal batchSizeKg, BigDecimal percent, BigDecimal sumPct) {
        // Nếu sumPct == 0 (bất thường) → ném lỗi
        if (sumPct == null || sumPct.signum() == 0) {
            throw new DataExistException("Tổng tỷ lệ thành phần bằng 0, không thể tính định mức.");
        }
        // q(g) = batch(kg) * (percent / sumPct) * 1000
        return batchSizeKg
                .multiply(percent)
                .divide(sumPct, 9, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(1000));
    }

    // helper cập nhật sức chứa location
    private void updateLocationCapacitySafe(Location location, double delta) {
        if (location == null || delta == 0d) return;
        Double cur = location.getCurrentCapacity() == null ? 0d : location.getCurrentCapacity();
        Double max = location.getMaxCapacity();
        double newCap = cur + delta;
        if (newCap < 0) newCap = 0d;
        location.setCurrentCapacity(newCap);
        if (max != null) {
            location.setIsAvailable(newCap < max);
        }
        locationRepo.saveAndFlush(location);
    }

}