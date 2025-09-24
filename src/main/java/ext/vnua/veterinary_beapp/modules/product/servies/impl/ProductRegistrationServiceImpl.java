package ext.vnua.veterinary_beapp.modules.product.servies.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductRegistrationDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productRegistration.CreateProductRegistrationRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productRegistration.GetProductRegistrationRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productRegistration.UpdateProductRegistrationRequest;
import ext.vnua.veterinary_beapp.modules.product.mapper.ProductRegistrationMapper;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import ext.vnua.veterinary_beapp.modules.product.model.ProductRegistration;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductRegistrationRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.custom.CustomProductRegistrationQuery;
import ext.vnua.veterinary_beapp.modules.product.servies.ProductRegistrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductRegistrationServiceImpl implements ProductRegistrationService {
    private final ProductRegistrationRepository regRepo;
    private final ProductRepository productRepo;
    private final ProductRegistrationMapper mapper;

    @Override
    public Page<ProductRegistration> filter(GetProductRegistrationRequest req, PageRequest pr) {
        Specification<ProductRegistration> spec = CustomProductRegistrationQuery.getFilter(req);
        return regRepo.findAll(spec, pr);
    }

    @Override
    public ProductRegistrationDto getByProduct(Long productId) {
        var r = regRepo.findByProductId(productId)
                .orElseThrow(() -> new DataExistException("Chưa có đăng ký sản phẩm"));
        return mapper.toDto(r);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "ProductRegistration", description = "Tạo mới đăng ký sản phẩm")
    public ProductRegistrationDto create(CreateProductRegistrationRequest req) {
        Product p = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new DataExistException("Sản phẩm không tồn tại"));
        if (regRepo.findByRegistrationNumber(req.getRegistrationNumber()).isPresent()) {
            throw new DataExistException("Số đăng ký đã tồn tại");
        }
        ProductRegistration r = mapper.toCreate(req);
        r.setProduct(p);
        return mapper.toDto(regRepo.saveAndFlush(r));
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductRegistration", description = "Cập nhật đăng ký sản phẩm")
    public ProductRegistrationDto update(UpdateProductRegistrationRequest req) {
        ProductRegistration r = regRepo.findById(req.getId())
                .orElseThrow(() -> new DataExistException("Đăng ký không tồn tại"));
        mapper.updateEntity(req, r);
        if (req.getProductId() != null) {
            r.setProduct(productRepo.findById(req.getProductId())
                    .orElseThrow(() -> new DataExistException("Sản phẩm không tồn tại")));
        }
        return mapper.toDto(regRepo.saveAndFlush(r));
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "ProductRegistration", description = "Xóa đăng ký sản phẩm")
    public void delete(Long id) {
        regRepo.deleteById(id);
    }
}
