package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.WarehouseTypeDto;
import ext.vnua.veterinary_beapp.modules.material.model.WarehouseType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface  WarehouseTypeMapper {
    WarehouseTypeDto toWarehouseTypeDto(WarehouseType wt);
}
