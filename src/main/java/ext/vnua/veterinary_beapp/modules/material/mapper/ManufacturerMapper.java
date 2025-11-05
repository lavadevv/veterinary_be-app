package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.ManufacturerDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.manufacturer.CreateManufacturerRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.manufacturer.UpdateManufacturerRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Manufacturer;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ManufacturerMapper {

    ManufacturerDto toManufacturerDto(Manufacturer entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true) // set ở service
    Manufacturer toCreateEntity(CreateManufacturerRequest req);

    // dùng khi update dạng replace
    Manufacturer toUpdateEntity(UpdateManufacturerRequest req);

    // dùng update “merge” vào entity hiện có
    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(UpdateManufacturerRequest req, @MappingTarget Manufacturer entity);
}
