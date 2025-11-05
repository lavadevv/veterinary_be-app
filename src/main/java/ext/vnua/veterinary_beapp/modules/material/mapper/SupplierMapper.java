package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.SupplierDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.supplier.CreateSupplierRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.supplier.UpdateSupplierRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Supplier;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    SupplierMapper INSTANCE = Mappers.getMapper(SupplierMapper.class);

    @Mapping(target = "manufacturerId", expression =
            "java(supplier.getManufacturer()!=null && org.hibernate.Hibernate.isInitialized(supplier.getManufacturer()) ? supplier.getManufacturer().getId() : null)")
    @Mapping(target = "manufacturerName", expression =
            "java(supplier.getManufacturer()!=null && org.hibernate.Hibernate.isInitialized(supplier.getManufacturer()) ? supplier.getManufacturer().getManufacturerName() : null)")
    SupplierDto toSupplierDto(Supplier supplier);

    Supplier toSupplier(SupplierDto supplierDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "materials", ignore = true)
    @Mapping(target = "isActive", ignore = true) // Will be set to true by default in service
    @Mapping(target = "manufacturer", ignore = true)
    Supplier toCreateSupplier(CreateSupplierRequest request);

    @Mapping(target = "materials", ignore = true)
    @Mapping(target = "manufacturer", ignore = true)
    Supplier toUpdateSupplier(UpdateSupplierRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "materials", ignore = true)
    @Mapping(target = "manufacturer", ignore = true)
    void updateSupplierFromRequest(UpdateSupplierRequest request, @MappingTarget Supplier supplier);

    @AfterMapping
    default void afterMappingCreate(@MappingTarget Supplier supplier, CreateSupplierRequest request) {
        // Set default values if not provided
        if (supplier.getIsActive() == null) {
            supplier.setIsActive(true);
        }
    }
}