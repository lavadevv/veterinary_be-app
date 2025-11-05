package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.SupplierDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.supplier.CreateSupplierRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.supplier.UpdateSupplierRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.SupplierMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Supplier;
import ext.vnua.veterinary_beapp.modules.material.repository.ManufacturerRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.SupplierRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomSupplierQuery;
import ext.vnua.veterinary_beapp.modules.material.service.SupplierService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final ManufacturerRepository manufacturerRepository;

    @Override
    public Page<Supplier> getAllSupplier(CustomSupplierQuery.SupplierFilterParam param, PageRequest pageRequest) {
        Specification<Supplier> specification = CustomSupplierQuery.getFilterSupplier(param);
        return supplierRepository.findAll(specification, pageRequest);
    }

    @Override
    public SupplierDto selectSupplierById(Long id) {
        Optional<Supplier> supplierOptional = supplierRepository.findById(id);
        if (supplierOptional.isEmpty()) {
            throw new DataExistException("Nhà cung cấp không tồn tại");
        }
        Supplier supplier = supplierOptional.get();
        return supplierMapper.toSupplierDto(supplier);
    }

    @Override
    public SupplierDto selectSupplierByCode(String supplierCode) {
        Optional<Supplier> supplierOptional = supplierRepository.findBySupplierCode(supplierCode);
        if (supplierOptional.isEmpty()) {
            throw new DataExistException("Mã nhà cung cấp không tồn tại");
        }
        Supplier supplier = supplierOptional.get();
        return supplierMapper.toSupplierDto(supplier);
    }

    @Override
    public List<SupplierDto> selectActiveSuppliers() {
        List<Supplier> suppliers = supplierRepository.findByIsActiveTrue();
        return suppliers.stream()
                .map(supplierMapper::toSupplierDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "Supplier", description = "Tạo mới nhà cung cấp")
    public SupplierDto createSupplier(CreateSupplierRequest request) {
        // Validate supplier code is unique
        Optional<Supplier> existingSupplier = supplierRepository
                .findBySupplierCode(request.getSupplierCode());
        if (existingSupplier.isPresent()) {
            throw new DataExistException("Mã nhà cung cấp đã tồn tại");
        }

        // Validate email if provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            Optional<Supplier> supplierWithEmail = supplierRepository.findByEmail(request.getEmail());
            if (supplierWithEmail.isPresent()) {
                throw new DataExistException("Email đã được sử dụng bởi nhà cung cấp khác");
            }
        }

        // Validate phone if provided
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            Optional<Supplier> supplierWithPhone = supplierRepository.findByPhone(request.getPhone());
            if (supplierWithPhone.isPresent()) {
                throw new DataExistException("Số điện thoại đã được sử dụng bởi nhà cung cấp khác");
            }
        }

        // Validate GMP expiry date
        if (request.getGmpExpiryDate() != null && request.getGmpExpiryDate().isBefore(LocalDate.now())) {
            throw new MyCustomException("Ngày hết hạn chứng chỉ GMP không được trong quá khứ");
        }

        try {
            Supplier supplier = supplierMapper.toCreateSupplier(request);
            supplier.setIsActive(true);

            if (request.getManufacturerId() != null) {
                var mf = manufacturerRepository.findById(request.getManufacturerId())
                        .orElseThrow(() -> new MyCustomException("Manufacturer không tồn tại"));
                supplier.setManufacturer(mf);
            } else {
                supplier.setManufacturer(null);
            }


            return supplierMapper.toSupplierDto(supplierRepository.saveAndFlush(supplier));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình thêm nhà cung cấp");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "Supplier", description = "Cập nhật nhà cung cấp")
    public SupplierDto updateSupplier(UpdateSupplierRequest request) {
        Optional<Supplier> supplierOptional = supplierRepository.findById(request.getId());
        if (supplierOptional.isEmpty()) {
            throw new DataExistException("Nhà cung cấp không tồn tại");
        }

        Supplier existingSupplier = supplierOptional.get();

        // Validate supplier code is unique (excluding current supplier)
        if (!existingSupplier.getSupplierCode().equals(request.getSupplierCode())) {
            Optional<Supplier> duplicateSupplier = supplierRepository
                    .findBySupplierCodeAndIdNot(request.getSupplierCode(), request.getId());
            if (duplicateSupplier.isPresent()) {
                throw new DataExistException("Mã nhà cung cấp đã tồn tại");
            }
        }

        // Validate email if changed and provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()
                && !request.getEmail().equals(existingSupplier.getEmail())) {
            Optional<Supplier> supplierWithEmail = supplierRepository.findByEmail(request.getEmail());
            if (supplierWithEmail.isPresent() && !supplierWithEmail.get().getId().equals(request.getId())) {
                throw new DataExistException("Email đã được sử dụng bởi nhà cung cấp khác");
            }
        }

        // Validate phone if changed and provided
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()
                && !request.getPhone().equals(existingSupplier.getPhone())) {
            Optional<Supplier> supplierWithPhone = supplierRepository.findByPhone(request.getPhone());
            if (supplierWithPhone.isPresent() && !supplierWithPhone.get().getId().equals(request.getId())) {
                throw new DataExistException("Số điện thoại đã được sử dụng bởi nhà cung cấp khác");
            }
        }

        // Validate GMP expiry date
        if (request.getGmpExpiryDate() != null && request.getGmpExpiryDate().isBefore(LocalDate.now())) {
            throw new MyCustomException("Ngày hết hạn chứng chỉ GMP không được trong quá khứ");
        }

        try {
            supplierMapper.updateSupplierFromRequest(request, existingSupplier);

            if (request.getManufacturerId() != null) {
                var mf = manufacturerRepository.findById(request.getManufacturerId())
                        .orElseThrow(() -> new MyCustomException("Manufacturer không tồn tại"));
                existingSupplier.setManufacturer(mf);
            } else {
                existingSupplier.setManufacturer(null);
            }

            return supplierMapper.toSupplierDto(supplierRepository.saveAndFlush(existingSupplier));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhật nhà cung cấp");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Supplier", description = "Xóa nhà cung cấp")
    public void deleteSupplier(Long id) {
        Optional<Supplier> supplierOptional = supplierRepository.findById(id);
        if (supplierOptional.isEmpty()) {
            throw new DataExistException("Nhà cung cấp không tồn tại");
        }

        Supplier supplier = supplierOptional.get();

        // Check if supplier has materials
        if (supplier.getMaterials() != null && !supplier.getMaterials().isEmpty()) {
            throw new MyCustomException("Không thể xóa nhà cung cấp đang có vật liệu");
        }

        try {
            supplierRepository.deleteById(id);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa nhà cung cấp");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "Supplier", description = "Xóa danh sách nhà cung cấp")
    public List<SupplierDto> deleteAllIdSuppliers(List<Long> ids) {
        List<SupplierDto> supplierDtos = new ArrayList<>();
        for (Long id : ids) {
            Optional<Supplier> optionalSupplier = supplierRepository.findById(id);
            if (optionalSupplier.isPresent()) {
                Supplier supplier = optionalSupplier.get();

                // Check if supplier has materials
                if (supplier.getMaterials() != null && !supplier.getMaterials().isEmpty()) {
                    throw new MyCustomException("Không thể xóa nhà cung cấp đang có vật liệu: " + supplier.getSupplierName());
                }

                supplierDtos.add(supplierMapper.toSupplierDto(supplier));
                supplierRepository.delete(supplier);
            } else {
                throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa danh sách nhà cung cấp!");
            }
        }
        return supplierDtos;
    }

    @Override
    @Transactional
    public void toggleActiveStatus(Long supplierId) {
        Optional<Supplier> supplierOptional = supplierRepository.findById(supplierId);
        if (supplierOptional.isEmpty()) {
            throw new DataExistException("Nhà cung cấp không tồn tại");
        }

        Supplier supplier = supplierOptional.get();
        supplier.setIsActive(!supplier.getIsActive());

        supplierRepository.saveAndFlush(supplier);
    }

    @Override
    public List<SupplierDto> getSuppliersWithExpiringGmp(int daysBeforeExpiry) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(daysBeforeExpiry);

        List<Supplier> suppliers = supplierRepository.findSuppliersWithExpiringGmp(startDate, endDate);
        return suppliers.stream()
                .map(supplierMapper::toSupplierDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<SupplierDto> getSuppliersWithExpiredGmp() {
        LocalDate currentDate = LocalDate.now();
        List<Supplier> suppliers = supplierRepository.findSuppliersWithExpiredGmp(currentDate);
        return suppliers.stream()
                .map(supplierMapper::toSupplierDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<SupplierDto> getSuppliersByCountry(String countryOfOrigin) {
        List<Supplier> suppliers = supplierRepository.findByCountryOfOrigin(countryOfOrigin);
        return suppliers.stream()
                .map(supplierMapper::toSupplierDto)
                .collect(java.util.stream.Collectors.toList());
    }
}
