package ext.vnua.veterinary_beapp.modules.product.services.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialRepository;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductFormulaDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.formula.ProductFormulaListRow;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.UpsertFormulaRequest;
import ext.vnua.veterinary_beapp.modules.product.mapper.ProductFormulaMapper;
import ext.vnua.veterinary_beapp.modules.product.model.*;
import ext.vnua.veterinary_beapp.modules.product.repository.FormulaHeaderRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductFormulaRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductRepository;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ProductFormulaServiceImpl — phiên bản dùng kiến trúc Header/Version
 * - Upsert: luôn tạo bản ghi phiên bản mới (snapshot)
 * - Active: kích hoạt 1 phiên bản và tự động deactivate các phiên bản khác cùng header
 * - Lấy theo productId: dựa vào header.products (ManyToMany)
 */
@Service
@RequiredArgsConstructor
public class ProductFormulaServiceImpl implements ProductFormulaService {

    private final ProductRepository productRepo;
    private final ProductFormulaRepository formulaRepo;
    private final ProductFormulaMapper mapper;
    private final MaterialRepository materialRepo;
    private final FormulaHeaderRepository headerRepo;

    /* ============================ CORE ============================ */

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "ProductFormula", description = "Thêm phiên bản công thức sản phẩm")
    public ProductFormulaDto upsertFormula(UpsertFormulaRequest req) {
        // 1) Header (bắt buộc có formulaCode)
        if (req.getFormulaCode() == null || req.getFormulaCode().isBlank()) {
            throw new DataExistException("Thiếu formulaCode");
        }
        FormulaHeader header = headerRepo.findByFormulaCode(req.getFormulaCode())
                .orElseGet(() -> {
                    FormulaHeader h = new FormulaHeader();
                    h.setFormulaCode(req.getFormulaCode());
                    h.setFormulaName(req.getFormulaName());
                    h.setDescription(req.getHeaderDescription());
                    return headerRepo.save(h);
                });

        // (tuỳ chọn) gán products cho header nếu có productIds trong request
        // REPLACE toàn bộ products (không ADD thêm vào list cũ)
        if (req.getProductIds() != null && !req.getProductIds().isEmpty()) {
            Set<Product> ps = new HashSet<>(productRepo.findAllById(req.getProductIds()));
            header.getProducts().clear(); // Xóa hết products cũ
            header.getProducts().addAll(ps); // Thêm products mới
            headerRepo.save(header);
        }

        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new DataExistException("Danh sách nguyên vật liệu trống");
        }

        // 2) Tạo phiên bản mới
        ProductFormula f = new ProductFormula();
        f.setHeader(header);
        f.setVersion(req.getVersion() != null && !req.getVersion().isBlank()
                ? req.getVersion()
                : DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now()));

        // ===== VALIDATION: Check duplicate version in same header =====
        boolean versionExists = formulaRepo.findAllVersions(header.getFormulaCode(), PageRequest.of(0, 1000))
                .stream()
                .anyMatch(existing -> existing.getVersion().equals(f.getVersion()));
        if (versionExists) {
            throw new DataExistException(
                    String.format("Version '%s' đã tồn tại cho công thức '%s'. Vui lòng sử dụng version khác.",
                            f.getVersion(), header.getFormulaCode())
            );
        }

        f.setBatchSize(req.getBatchSize());
        f.setDescription(emptyToNull(req.getDescription()));
        f.setSopFilePath(emptyToNull(req.getSopFilePath()));
        f.setIsActive(req.getIsActive() != null ? req.getIsActive() : Boolean.TRUE);
        f.setChangeNote(emptyToNull(req.getChangeNote()));

        // Cờ & cơ sở tính
        f.setIsLiquidFormula(Boolean.TRUE.equals(req.getIsLiquidFormula()));
        f.setBasisValue(req.getBasisValue());
        f.setBasisUnit(req.getBasisUnit());
        f.setDensity(req.getDensity());

        // 3) Reset danh sách item
        if (f.getFormulaItems() == null) f.setFormulaItems(new ArrayList<>());
        else f.getFormulaItems().clear();

        // 4) Tính toán nội dung hoạt chất/định mức
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
            // Load Material with activeIngredients eagerly using EntityGraph (already configured in repo)
            Material material = materialRepo.findById(it.getMaterialId())
                    .orElseThrow(() -> new DataExistException("Material không tồn tại: ID=" + it.getMaterialId()));
            
            // EntityGraph in MaterialRepository already loads activeIngredients and nested activeIngredient entities
            // No need for manual force loading
            
            // Hàm lượng hoạt chất: iuPerGram (mg/g) - LEGACY field, sẽ dần bỏ khi dùng activeIngredients
            BigDecimal iuPerGram = material.getIuPerGram();
            if (iuPerGram == null || iuPerGram.signum() < 0) iuPerGram = BigDecimal.ZERO;

            ProductFormulaItem e = new ProductFormulaItem();
            e.setFormula(f);
            e.setMaterial(material);
            e.setIsCritical(Boolean.TRUE.equals(it.getIsCritical()));
            e.setNotes(emptyToNull(it.getNotes()));

            // Lưu label (optional)
            e.setLabelAmount(it.getLabelAmount());
            e.setLabelUnit(it.getLabelUnit());

            BigDecimal formulaMg; // kết quả hàm lượng công thức (mg)

            if (hasPercent) {
                // Lưu theo % → gramsOfMaterial = basisGram * (%/100); mg = (mg/g) * grams
                e.setPercentage(it.getPercentage());
                e.setQuantity(null);
                e.setUnit(null);
                sumPct = sumPct.add(it.getPercentage());

                BigDecimal gramsOfMaterial = basisGram.multiply(
                        it.getPercentage().divide(ONE_HUNDRED, DIV_SCALE, RoundingMode.HALF_UP)
                );
                formulaMg = iuPerGram.multiply(gramsOfMaterial);
            } else {
                // Lưu theo định mức tuyệt đối: iuPerGram(mg/g) * grams => mg
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

            // % đạt = formula(mg) * 100 / label(mg)  (làm tròn 1 chữ số)
            if (it.getLabelAmount() != null && it.getLabelAmount().signum() > 0) {
                BigDecimal labelInMg = toMgSafe(it.getLabelAmount(), it.getLabelUnit());
                if (labelInMg != null && labelInMg.signum() > 0) {
                    BigDecimal achieved = formulaMg.multiply(ONE_HUNDRED)
                            .divide(labelInMg, DIV_SCALE, RoundingMode.HALF_UP);
                    e.setAchievedPercent(round1(achieved)); // 1 chữ số
                } else {
                    e.setAchievedPercent(null);
                }
            }

            // ===== NEW: Populate Active Ingredients tracking =====
            // Tính gramsOfMaterial để dùng cho tất cả activeIngredients
            BigDecimal gramsOfMaterial;
            if (hasPercent) {
                gramsOfMaterial = basisGram.multiply(
                        e.getPercentage().divide(ONE_HUNDRED, DIV_SCALE, RoundingMode.HALF_UP)
                );
            } else {
                String unit = e.getUnit().trim().toLowerCase();
                gramsOfMaterial = unit.equals("kg")
                        ? e.getQuantity().multiply(THOUSAND)
                        : e.getQuantity(); // g
            }
            
            // Load activeIngredients from Material and create tracking records
            if (material.getActiveIngredients() != null && !material.getActiveIngredients().isEmpty()) {
                for (var mai : material.getActiveIngredients()) {
                    ProductFormulaItemActiveIngredient aiTracking = new ProductFormulaItemActiveIngredient();
                    aiTracking.setFormulaItem(e);
                    
                    // EntityGraph already loaded activeIngredient - safe to access directly
                    Long aiId = null;
                    String aiName = "Unknown";
                    if (mai.getActiveIngredient() != null) {
                        aiId = mai.getActiveIngredient().getId();
                        aiName = mai.getActiveIngredient().getIngredientName();
                    }
                    
                    aiTracking.setActiveIngredientId(aiId);
                    aiTracking.setActiveIngredientName(aiName);
                    aiTracking.setContentValue(mai.getContentValue());
                    aiTracking.setContentUnit(mai.getContentUnit());
                    
                    // Initialize label fields (will be filled by user later from frontend request if provided)
                    // Check if request has activeIngredients data
                    if (it.getActiveIngredients() != null) {
                        var reqAi = it.getActiveIngredients().stream()
                                .filter(a -> a.getActiveIngredientId() != null && 
                                        a.getActiveIngredientId().equals(mai.getActiveIngredient().getId()))
                                .findFirst()
                                .orElse(null);
                        if (reqAi != null) {
                            aiTracking.setLabelAmount(reqAi.getLabelAmount());
                            aiTracking.setLabelUnit(reqAi.getLabelUnit());
                            aiTracking.setNotes(reqAi.getNotes());
                        }
                    }
                    
                    // Calculate formulaContentAmount
                    // contentValue is in unit per gram (e.g., mg/g, IU/g)
                    // Formula: contentValue × gramsOfMaterial
                    if (mai.getContentValue() != null && gramsOfMaterial != null) {
                        BigDecimal formulaContent = mai.getContentValue().multiply(gramsOfMaterial);
                        aiTracking.setFormulaContentAmount(formulaContent);
                        aiTracking.setFormulaContentUnit(extractFormulaUnit(mai.getContentUnit())); // Extract "mg" from "mg/g"
                        
                        // Calculate achievedPercent if labelAmount is provided
                        if (aiTracking.getLabelAmount() != null && aiTracking.getLabelAmount().signum() > 0) {
                            BigDecimal labelInBaseUnit = toMgSafe(aiTracking.getLabelAmount(), aiTracking.getLabelUnit());
                            if (labelInBaseUnit != null && labelInBaseUnit.signum() > 0) {
                                BigDecimal achieved = formulaContent.multiply(ONE_HUNDRED)
                                        .divide(labelInBaseUnit, DIV_SCALE, RoundingMode.HALF_UP);
                                aiTracking.setAchievedPercent(achieved);
                            }
                        }
                    }
                    
                    e.getActiveIngredients().add(aiTracking);
                }
            }
            // If material has NO active ingredients, that's OK - just leave the list empty
            // Frontend will show warning but still allow saving

            f.getFormulaItems().add(e);
        }

        // 5) Ràng buộc tổng % (nếu có nhập %)
        if (!Boolean.TRUE.equals(f.getIsLiquidFormula())
                && sumPct.signum() > 0
                && sumPct.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new DataExistException("Tổng % vượt quá 100% (chỉ cho phép >100 nếu là công thức dung dịch). Tổng hiện tại = " + sumPct);
        }

        ProductFormula saved = formulaRepo.saveAndFlush(f);

        // Nếu phiên bản này active → hạ active các phiên bản khác cùng header
        if (Boolean.TRUE.equals(saved.getIsActive())) {
            formulaRepo.deactivateOthers(header.getId(), saved.getId());
        }
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public ProductFormulaDto getActiveFormula(Long productId) {
        // Lấy tất cả header gắn với productId
        var headers = headerRepo.searchHeaders(null, productId, PageRequest.of(0, 100)).getContent();
        if (headers.isEmpty()) throw new DataExistException("Sản phẩm chưa gắn công thức");

        // Ưu tiên phiên bản active mới nhất trong từng header
        for (var h : headers) {
            var page = formulaRepo.findAllVersions(h.getFormulaCode(), PageRequest.of(0, 20));
            Optional<ProductFormula> activeLatest = page.stream()
                    .filter(ProductFormula::getIsActive)
                    .findFirst();
            if (activeLatest.isPresent()) return mapper.toDto(activeLatest.get());
        }

        // Fallback: lấy phiên bản mới nhất của header đầu tiên (theo createdDate desc)
        var page = formulaRepo.findAllVersions(headers.get(0).getFormulaCode(), PageRequest.of(0, 1));
        if (page.isEmpty()) throw new DataExistException("Chưa có phiên bản công thức");
        return mapper.toDto(page.getContent().get(0));
    }

    @Override
    @Transactional
    public List<ProductFormulaDto> listFormulas(Long productId) {
        // Trả "latest per header" gắn với productId
        var headers = headerRepo.searchHeaders(null, productId, PageRequest.of(0, 500)).getContent();
        if (headers.isEmpty()) return List.of();
        var latest = formulaRepo.findLatestByHeaderIds(headers.stream().map(FormulaHeader::getId).toList());
        return latest.stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductFormula", description = "Kích hoạt công thức (1 header chỉ 1 active)")
    public void activateFormula(Long formulaId) {
        ProductFormula f = formulaRepo.findById(formulaId)
                .orElseThrow(() -> new DataExistException("Công thức không tồn tại"));
        if (f.getHeader() == null) throw new DataExistException("Công thức chưa gắn header");

        // Đặt active cho bản hiện tại, hạ các bản khác
        f.setIsActive(true);
        formulaRepo.save(f);
        formulaRepo.deactivateOthers(f.getHeader().getId(), f.getId());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "ProductFormula", description = "Xóa công thức sản phẩm")
    public void deleteFormula(Long formulaId) {
        formulaRepo.deleteById(formulaId);
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
            // Lấy product đại diện từ header.products (nếu có)
            Product rep = pickFirstProduct(f);
            Long productId = rep != null ? rep.getId() : null;
            String productCode = rep != null ? rep.getProductCode() : null;
            String productName = rep != null ? rep.getProductName() : null;
            var productCategory = rep != null ? rep.getProductCategory() : null;
            var formulationType = rep != null ? rep.getFormulationType() : null;

            long total = (f.getFormulaItems() == null) ? 0 : f.getFormulaItems().size();
            long critical = (f.getFormulaItems() == null) ? 0 :
                    f.getFormulaItems().stream().filter(it -> Boolean.TRUE.equals(it.getIsCritical())).count();

            return new ProductFormulaListRow(
                    f.getId(),
                    (f.getHeader() != null ? f.getHeader().getFormulaCode() : null),
                    (f.getHeader() != null ? f.getHeader().getFormulaName() : null),
                    productId,
                    productCode,
                    productName,
                    f.getVersion(),
                    f.getIsActive(),
                    f.getBatchSize(),
                    total,
                    critical,
                    f.getCreatedDate(),
                    f.getCreatedBy(),
                    productCategory,
                    formulationType
            );
        }).toList();

        return new PageImpl<>(rows, page.getPageable(), page.getTotalElements());
    }

    /* ============================ HELPERS ============================ */

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    static BigDecimal round1(BigDecimal v) {
        return v == null ? null : v.setScale(1, RoundingMode.HALF_UP);
    }

    private static BigDecimal toMgSafe(BigDecimal value, String unit) {
        if (value == null) return null;
        String u = (unit == null ? "mg" : unit.trim().toLowerCase());
        return switch (u) {
            case "mg" -> value;
            case "g"  -> value.multiply(BigDecimal.valueOf(1_000));
            case "kg" -> value.multiply(BigDecimal.valueOf(1_000_000));
            default   -> null;
        };
    }

    /**
     * Extract base unit from compound unit string
     * Examples: "mg/g" → "mg", "IU/g" → "IU", "%" → "%"
     */
    private static String extractFormulaUnit(String contentUnit) {
        if (contentUnit == null || contentUnit.isBlank()) return "mg";
        String unit = contentUnit.trim();
        // Split by "/" and take first part
        if (unit.contains("/")) {
            return unit.split("/")[0].trim();
        }
        return unit;
    }

    private static Product pickFirstProduct(ProductFormula f) {
        if (f == null || f.getHeader() == null || f.getHeader().getProducts() == null) return null;
        return f.getHeader().getProducts().stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public List<ProductFormulaDto> getCatalogLatest(String q, Long productId, int start, int limit) {
        var headersPage = headerRepo.searchHeaders(q, productId, PageRequest.of(start, limit));
        var headerIds = headersPage.getContent().stream().map(FormulaHeader::getId).toList();
        if (headerIds.isEmpty()) return List.of();
        var latest = formulaRepo.findLatestByHeaderIdsWithItemsAndProducts(headerIds);
        return latest.stream().map(mapper::toDto).toList();
    }
}
