package ext.vnua.veterinary_beapp.modules.users.services;

import ext.vnua.veterinary_beapp.modules.users.dto.entity.UserDto;
import ext.vnua.veterinary_beapp.modules.users.dto.request.CreateUserRequest;
import ext.vnua.veterinary_beapp.modules.users.dto.request.UpdateUserRequest;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import ext.vnua.veterinary_beapp.modules.users.repository.CustomUserQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface UserService {
    Page<User> getAllUser(CustomUserQuery.UserFilterParam param, PageRequest pageRequest);
    UserDto selectUserByEmail(String email);
    UserDto selectUserById(Long id);
    void changeAvatar(String email, byte[] fileBytes);

    UserDto createUser(CreateUserRequest request);
    UserDto updateUser(UpdateUserRequest request);

    void deleteUser(Long id);
    List<UserDto> deleteAllIdUsers(List<Long> ids);
}
