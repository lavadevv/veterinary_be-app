package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.StockAlertDto;
import ext.vnua.veterinary_beapp.modules.material.model.StockAlert;
import ext.vnua.veterinary_beapp.modules.users.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {MaterialMapper.class, MaterialBatchMapper.class, UserMapper.class})
public interface StockAlertMapper {
    StockAlertMapper INSTANCE = Mappers.getMapper(StockAlertMapper.class);

    @Mapping(source = "material", target = "materialDto")
    @Mapping(source = "materialBatch", target = "materialBatchDto")
    @Mapping(source = "resolvedBy", target = "resolvedByDto")
    StockAlertDto toStockAlertDto(StockAlert stockAlert);

    @Mapping(source = "materialDto", target = "material")
    @Mapping(source = "materialBatchDto", target = "materialBatch")
    @Mapping(source = "resolvedByDto", target = "resolvedBy")
    StockAlert toStockAlert(StockAlertDto stockAlertDto);
}