package ext.vnua.veterinary_beapp.modules.users.mapper;

import ext.vnua.veterinary_beapp.modules.users.dto.entity.UserDto;
import ext.vnua.veterinary_beapp.modules.users.dto.request.CreateUserRequest;
import ext.vnua.veterinary_beapp.modules.users.dto.request.UpdateUserRequest;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Map từ UpdateUserRequest -> User (department/position set ở service theo ID)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "position", ignore = true)
    User toUpdateUser(UpdateUserRequest userRequest);

    // Map từ CreateUserRequest -> User (department/position set ở service theo ID)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "position", ignore = true)
    User toCreateUser(CreateUserRequest userRequest);

    // Chỉ map ID (KHÔNG đụng vào .getName() để tránh LazyInitialization)
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "positionId", source = "position.id")
    UserDto toUserDto(User user);

    // Ngược lại thường không dùng trực tiếp (cần set master ở service)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toUser(UserDto userDto);
}
