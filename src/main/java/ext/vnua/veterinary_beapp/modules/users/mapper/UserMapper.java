package ext.vnua.veterinary_beapp.modules.users.mapper;

import ext.vnua.veterinary_beapp.modules.users.dto.entity.UserDto;
import ext.vnua.veterinary_beapp.modules.users.dto.request.CreateUserRequest;
import ext.vnua.veterinary_beapp.modules.users.dto.request.UpdateUserRequest;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password",ignore = true)
    @Mapping(target = "role",ignore = true)
    User toUpdateUser(UpdateUserRequest userRequest);

    @Mapping(target = "password",ignore = true)
    @Mapping(target = "role",ignore = true)
    User toCreateUser(CreateUserRequest userRequest);

    //@Mapping(target = "role",ignore = true)
    UserDto toUserDto(User user);

    User toUser(UserDto userDto);
}
