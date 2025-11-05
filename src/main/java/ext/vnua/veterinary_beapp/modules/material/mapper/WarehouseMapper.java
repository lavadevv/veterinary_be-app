package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.WarehouseDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.warehouse.CreateWarehouseRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.warehouse.UpdateWarehouseRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Warehouse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    /* ========== Entity -> DTO (map id quan hệ) ========== */
    @Mapping(target = "warehouseTypeId", source = "warehouseType.id")
    WarehouseDto toWarehouseDto(Warehouse warehouse);

    /* ========== DTO -> Entity (ít dùng trực tiếp) ========== */
    @Mapping(target = "warehouseType", ignore = true) // set ở service theo ID
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    Warehouse toWarehouse(WarehouseDto warehouseDto);

    /* ========== CreateRequest -> Entity ========== */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "warehouseType", ignore = true) // set ở service theo warehouseTypeId
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "isActive", ignore = true)      // set default ở service
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    Warehouse toCreateWarehouse(CreateWarehouseRequest request);

    /* ========== UpdateRequest -> Entity (ít dùng, ưu tiên update mapping) ========== */
    @Mapping(target = "warehouseType", ignore = true)
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    Warehouse toUpdateWarehouse(UpdateWarehouseRequest request);

    /* ========== Update in-place ========== */
    @Mapping(target = "warehouseType", ignore = true)
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    void updateWarehouseFromRequest(UpdateWarehouseRequest request, @MappingTarget Warehouse warehouse);
}
