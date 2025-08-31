package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.SupplierDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.supplier.CreateSupplierRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.supplier.UpdateSupplierRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Supplier;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomSupplierQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface SupplierService {
    Page<Supplier> getAllSupplier(CustomSupplierQuery.SupplierFilterParam param, PageRequest pageRequest);
    SupplierDto selectSupplierById(Long id);
    SupplierDto selectSupplierByCode(String supplierCode);
    List<SupplierDto> selectActiveSuppliers();

    SupplierDto createSupplier(CreateSupplierRequest request);
    SupplierDto updateSupplier(UpdateSupplierRequest request);

    void deleteSupplier(Long id);
    List<SupplierDto> deleteAllIdSuppliers(List<Long> ids);

    // Additional business methods
    void toggleActiveStatus(Long supplierId);
    List<SupplierDto> getSuppliersWithExpiringGmp(int daysBeforeExpiry);
    List<SupplierDto> getSuppliersWithExpiredGmp();
    List<SupplierDto> getSuppliersByCountry(String countryOfOrigin);
}
