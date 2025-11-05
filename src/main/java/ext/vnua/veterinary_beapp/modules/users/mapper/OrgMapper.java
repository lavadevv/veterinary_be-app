package ext.vnua.veterinary_beapp.modules.users.mapper;

import ext.vnua.veterinary_beapp.modules.users.dto.entity.DepartmentDto;
import ext.vnua.veterinary_beapp.modules.users.dto.entity.PositionDto;
import ext.vnua.veterinary_beapp.modules.users.model.Department;
import ext.vnua.veterinary_beapp.modules.users.model.Position;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrgMapper {
    DepartmentDto toDepartmentDto(Department e);
    PositionDto toPositionDto(Position e);
}
