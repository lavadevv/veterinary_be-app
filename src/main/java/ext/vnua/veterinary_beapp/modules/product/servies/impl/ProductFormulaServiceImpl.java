package ext.vnua.veterinary_beapp.modules.product.servies.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductFormulaDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.UpsertFormulaRequest;
import ext.vnua.veterinary_beapp.modules.product.mapper.ProductFormulaMapper;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormula;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormulaItem;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductFormulaRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductRepository;

// 1) import thêm:
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialRepository;

import ext.vnua.veterinary_beapp.modules.product.servies.ProductFormulaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFormulaServiceImpl implements ProductFormulaService {

    private final ProductRepository productRepo;
    private final ProductFormulaRepository formulaRepo;
    private final ProductFormulaMapper mapper;

    // 2) inject repo material
    private final MaterialRepository materialRepo;

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "ProductFormula", description = "Thêm hoặc cập nhật công thức sản phẩm")
    public ProductFormulaDto upsertFormula(UpsertFormulaRequest req) {
        Product p = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new DataExistException("Sản phẩm không tồn tại"));

        // Không dùng mapper.toEntity ở nhánh create để tránh list null.
        ProductFormula f = formulaRepo.findByProductIdAndVersion(req.getProductId(), req.getVersion())
                .orElseGet(ProductFormula::new);

        f.setProduct(p);
        f.setVersion(req.getVersion());
        f.setBatchSize(req.getBatchSize());
        f.setDescription(req.getDescription());
        f.setSopFilePath(req.getSopFilePath());
        f.setIsActive(req.getIsActive() != null ? req.getIsActive() : Boolean.TRUE);

        // ensure list không null
        if (f.getFormulaItems() == null) {
            f.setFormulaItems(new ArrayList<>());
        } else {
            f.getFormulaItems().clear();
        }

        // validate request items
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new DataExistException("Danh sách nguyên vật liệu trống");
        }

        for (var it : req.getItems()) {
            if (it.getMaterialId() == null) {
                throw new DataExistException("Thiếu materialId cho một dòng NVL");
            }
            if (it.getQuantity() == null || it.getQuantity().signum() <= 0) {
                throw new DataExistException("Quantity phải > 0");
            }
            if (it.getUnit() == null || it.getUnit().trim().isEmpty()) {
                throw new DataExistException("Unit là bắt buộc");
            }

            ProductFormulaItem e = new ProductFormulaItem();

            // *** CHỐT: set material entity từ materialId
            Material materialRef = materialRepo.getReferenceById(it.getMaterialId());
            e.setMaterial(materialRef);

            e.setFormula(f);
            e.setQuantity(it.getQuantity());
            e.setUnit(it.getUnit().trim());
            e.setPercentage(it.getPercentage());
            e.setIsCritical(Boolean.TRUE.equals(it.getIsCritical()));
            e.setNotes((it.getNotes() == null || it.getNotes().isBlank()) ? null : it.getNotes().trim());

            f.getFormulaItems().add(e);
        }

        return mapper.toDto(formulaRepo.saveAndFlush(f));
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
}
