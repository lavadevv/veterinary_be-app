package ext.vnua.veterinary_beapp.modules.product.services.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductFormulaDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.formula.ProductFormulaListRow;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.UpsertFormulaRequest;
import ext.vnua.veterinary_beapp.modules.product.mapper.ProductFormulaMapper;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormula;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormulaItem;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductFormulaRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductRepository;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.custom.CustomProductFormulaQuery;
import ext.vnua.veterinary_beapp.modules.product.services.ProductFormulaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductFormulaServiceImpl implements ProductFormulaService {

    private final ProductRepository productRepo;
    private final ProductFormulaRepository formulaRepo;
    private final ProductFormulaMapper mapper;
    private final MaterialRepository materialRepo;

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "ProductFormula", description = "Thêm hoặc cập nhật công thức sản phẩm")
    public ProductFormulaDto upsertFormula(UpsertFormulaRequest req) {
        // 1) Validate product
        Product p = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new DataExistException("Sản phẩm không tồn tại"));

        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new DataExistException("Danh sách nguyên vật liệu trống");
        }

        // 2) Lấy/khởi tạo công thức
        ProductFormula f = formulaRepo.findByProductIdAndVersion(req.getProductId(), req.getVersion())
                .orElseGet(ProductFormula::new);

        f.setProduct(p);
        f.setVersion(req.getVersion());
        f.setBatchSize(req.getBatchSize());
        f.setDescription(emptyToNull(req.getDescription()));
        f.setSopFilePath(emptyToNull(req.getSopFilePath()));
        f.setIsActive(req.getIsActive() != null ? req.getIsActive() : Boolean.TRUE);

        // Giữ cờ dung dịch để ràng buộc tổng %
        f.setIsLiquidFormula(Boolean.TRUE.equals(req.getIsLiquidFormula()));
        f.setBasisValue(req.getBasisValue()); // không còn dùng vào tính toán nhưng giữ để hiển thị nếu cần
        f.setBasisUnit(req.getBasisUnit());
        f.setDensity(req.getDensity());

        // 3) Reset danh sách item
        if (f.getFormulaItems() == null) f.setFormulaItems(new ArrayList<>());
        else f.getFormulaItems().clear();

        // 4) Tính toán
        int lineNo = 0;
        BigDecimal sumPct = BigDecimal.ZERO;

        final int DIV_SCALE = 6;
        final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
        final BigDecimal THOUSAND = BigDecimal.valueOf(1000);

        // Cơ sở quy đổi về GRAM (mặc định 1000 g)
        BigDecimal basisValue = (req.getBasisValue() == null ? THOUSAND : req.getBasisValue());
        String basisUnit = (req.getBasisUnit() == null || req.getBasisUnit().isBlank())
                ? "g" : req.getBasisUnit().trim().toLowerCase();

        BigDecimal basisGram = switch (basisUnit) {
            case "g"  -> basisValue;                         // g
            case "kg" -> basisValue.multiply(THOUSAND);      // kg -> g
            case "l"  -> {                                   // L -> g (nếu có density, g/mL)
                if (req.getDensity() == null || req.getDensity().signum() <= 0) {
                    yield THOUSAND;                          // mặc định 1L ~ 1000g (nước)
                } else {
                    yield req.getDensity().multiply(THOUSAND); // density(g/mL) * 1000 mL
                }
            }
            default   -> throw new DataExistException("basisUnit không hợp lệ: " + basisUnit);
        };

        for (var it : req.getItems()) {
            lineNo++;
            if (it.getMaterialId() == null) {
                throw new DataExistException("Thiếu materialId ở dòng #" + lineNo);
            }

            boolean hasPercent = it.getPercentage() != null && it.getPercentage().signum() > 0;
            boolean hasAbs = it.getQuantity() != null && it.getQuantity().signum() > 0
                    && it.getUnit() != null && !it.getUnit().isBlank();

            if (!hasPercent && !hasAbs) {
                throw new DataExistException("Dòng #" + lineNo + " phải nhập % hoặc quantity + unit");
            }
            if (hasPercent && hasAbs) {
                throw new DataExistException("Dòng #" + lineNo + " không được nhập đồng thời % và quantity");
            }

            Material materialRef = materialRepo.getReferenceById(it.getMaterialId());
            // === HÀM LƯỢNG HOẠT CHẤT: iuPerGram (mg/g) ===
            BigDecimal iuPerGram = materialRef.getIuPerGram();
            if (iuPerGram == null || iuPerGram.signum() < 0) iuPerGram = BigDecimal.ZERO;

            ProductFormulaItem e = new ProductFormulaItem();
            e.setFormula(f);
            e.setMaterial(materialRef);
            e.setIsCritical(Boolean.TRUE.equals(it.getIsCritical()));
            e.setNotes(emptyToNull(it.getNotes()));

            // Lưu label (optional)
            e.setLabelAmount(it.getLabelAmount());
            e.setLabelUnit(it.getLabelUnit());

            BigDecimal formulaMg; // kết quả hàm lượng công thức (mg)

            if (hasPercent) {
                // Lưu theo %
                e.setPercentage(it.getPercentage());
                e.setQuantity(null);
                e.setUnit(null);
                sumPct = sumPct.add(it.getPercentage());

                // === CÔNG THỨC ĐÚNG: mg = (mg/g) * (g NVL) ; g NVL = basisGram * (%/100)
                BigDecimal gramsOfMaterial = basisGram.multiply(
                        it.getPercentage().divide(ONE_HUNDRED, DIV_SCALE, RoundingMode.HALF_UP)
                );
                formulaMg = iuPerGram.multiply(gramsOfMaterial);

            } else {
                // Lưu theo định mức tuyệt đối (giữ như cũ): iuPerGram(mg/g) * grams => mg
                String unit = it.getUnit().trim().toLowerCase();
                if (!Objects.equals(unit, "g") && !Objects.equals(unit, "kg")) {
                    throw new DataExistException("Chưa hỗ trợ đơn vị định mức: " + it.getUnit());
                }
                e.setQuantity(it.getQuantity());
                e.setUnit(unit);
                e.setPercentage(null);

                BigDecimal grams = unit.equals("kg")
                        ? it.getQuantity().multiply(THOUSAND)
                        : it.getQuantity(); // g

                formulaMg = iuPerGram.multiply(grams);
            }

            e.setFormulaContentAmount(formulaMg);
            e.setFormulaContentUnit("mg");

            // % đạt = formula * 100 / label
            // % đạt = formula(mg) * 100 / label(mg)  (làm tròn 1 chữ số)
            if (it.getLabelAmount() != null && it.getLabelAmount().signum() > 0) {
                BigDecimal labelInMg = toMgSafe(it.getLabelAmount(), it.getLabelUnit()); // <— new
                if (labelInMg != null && labelInMg.signum() > 0) {
                    BigDecimal achieved = formulaMg.multiply(ONE_HUNDRED)
                            .divide(labelInMg, DIV_SCALE, RoundingMode.HALF_UP);
                    e.setAchievedPercent(round1(achieved)); // 1 chữ số
                } else {
                    // không hỗ trợ đơn vị, bỏ qua tính % đạt
                    e.setAchievedPercent(null);
                }
            }


            f.getFormulaItems().add(e);
        }

        // 5) Ràng buộc tổng % (nếu có nhập %)
        if (!Boolean.TRUE.equals(f.getIsLiquidFormula())
                && sumPct.signum() > 0
                && sumPct.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new DataExistException("Tổng % vượt quá 100% (chỉ cho phép >100 nếu là công thức dung dịch). Tổng hiện tại = " + sumPct);
        }

        ProductFormula saved = formulaRepo.saveAndFlush(f);
        return mapper.toDto(saved);
    }



    @Override
    public ProductFormulaDto getActiveFormula(Long productId) {
        var f = formulaRepo.findFirstActiveByProductIdWithProduct(productId)
                .orElseThrow(() -> new DataExistException("Chưa có công thức active"));
        return mapper.toDto(f);
    }

    @Override
    public List<ProductFormulaDto> listFormulas(Long productId) {
        return formulaRepo.findByProductIdWithProductOrderByCreatedDateDesc(productId)
                .stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductFormula", description = "Kích hoạt công thức sản phẩm")
    public void activateFormula(Long formulaId) {
        ProductFormula f = formulaRepo.findById(formulaId)
                .orElseThrow(() -> new DataExistException("Công thức không tồn tại"));

        var list = formulaRepo.findByProductIdWithProductOrderByCreatedDateDesc(f.getProduct().getId());
        for (ProductFormula x : list) {
            x.setIsActive(x.getId().equals(f.getId()));
        }
        formulaRepo.saveAll(list);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "ProductFormula", description = "Xóa công thức sản phẩm")
    public void deleteFormula(Long formulaId) {
        formulaRepo.deleteById(formulaId);
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    @Override
    @Transactional
    public ProductFormulaDto getById(Long formulaId) {
        var f = formulaRepo.findById(formulaId)
                .orElseThrow(() -> new DataExistException("Công thức không tồn tại"));
        return mapper.toDto(f);
    }

    @Override
    @Transactional
    public Page<ProductFormula> getAllFormulas(CustomProductFormulaQuery.ProductFormulaFilterParam param, PageRequest pageRequest) {
        var spec = CustomProductFormulaQuery.getFilter(param);
        return formulaRepo.findAll(spec, pageRequest);
    }

    @Override
    @Transactional
    public Page<ProductFormulaListRow> getAllFormulaRows(CustomProductFormulaQuery.ProductFormulaFilterParam param, PageRequest pageRequest) {
        Page<ProductFormula> page = getAllFormulas(param, pageRequest);

        var rows = page.getContent().stream().map(f -> {
            var p = f.getProduct();
            long total = (f.getFormulaItems() == null) ? 0 : f.getFormulaItems().size();
            long critical = (f.getFormulaItems() == null) ? 0 :
                    f.getFormulaItems().stream().filter(it -> Boolean.TRUE.equals(it.getIsCritical())).count();

            return new ProductFormulaListRow(
                    f.getId(),
                    p.getId(),
                    p.getProductCode(),
                    p.getProductName(),
                    f.getVersion(),
                    f.getIsActive(),
                    f.getBatchSize(),
                    total,
                    critical,
                    f.getCreatedDate(),
                    f.getCreatedBy(),
                    p.getBrandName(),
                    p.getProductCategory(),
                    p.getFormulationType()
            );
        }).toList();

        return new PageImpl<>(rows, page.getPageable(), page.getTotalElements());
    }

    // ===== Helpers =====

    static BigDecimal round1(BigDecimal v) {
        return v == null ? null : v.setScale(1, RoundingMode.HALF_UP);
    }

    private static BigDecimal toMgSafe(BigDecimal value, String unit) {
        if (value == null) return null;
        String u = (unit == null ? "mg" : unit.trim().toLowerCase());
        return switch (u) {
            case "mg" -> value;
            case "g" -> value.multiply(BigDecimal.valueOf(1_000));
            case "kg" -> value.multiply(BigDecimal.valueOf(1_000_000));
            default ->
                    null;
        };
    }


}
