package ext.vnua.veterinary_beapp.modules.users.services.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.audits.service.AuditService;
import ext.vnua.veterinary_beapp.modules.users.dto.entity.UserDto;
import ext.vnua.veterinary_beapp.modules.users.dto.request.CreateUserRequest;
import ext.vnua.veterinary_beapp.modules.users.dto.request.UpdateUserRequest;
import ext.vnua.veterinary_beapp.modules.users.mapper.UserMapper;
import ext.vnua.veterinary_beapp.modules.users.model.Role;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import ext.vnua.veterinary_beapp.modules.users.repository.CustomUserQuery;
import ext.vnua.veterinary_beapp.modules.users.repository.RoleRepository;
import ext.vnua.veterinary_beapp.modules.users.repository.UserRepository;
import ext.vnua.veterinary_beapp.modules.users.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Override
    public Page<User> getAllUser(CustomUserQuery.UserFilterParam param, PageRequest pageRequest) {
        Specification<User> specification = CustomUserQuery.getFilterUser(param);
        return userRepository.findAll(specification, pageRequest);
    }

    @Override
    public UserDto selectUserByEmail(String email) {
        Optional<User> userOptional=userRepository.findByEmail(email);
        if(!userOptional.isPresent()){
            throw new DataExistException("Email không tồn tại");
        }
        User user=userOptional.get();
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto selectUserById(Long id) {
        Optional<User> userOptional=userRepository.findById(id);
        if(!userOptional.isPresent()){
            throw new DataExistException("Người dùng không tồn tại");
        }
        User user=userOptional.get();
        return userMapper.toUserDto(user);
    }

    @Override
    public void changeAvatar(String email, byte[] fileBytes) {
        Optional<User> userOptional=userRepository.findByEmail(email);
        if(!userOptional.isPresent()){
            throw new DataExistException("Email không tồn tại");
        }
        User user=userOptional.get();
        user.setB64(Base64.getEncoder().encodeToString(fileBytes));
        userRepository.saveAndFlush(user);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "User", description = "Tạo mới người dùng")
    public UserDto createUser(CreateUserRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isPresent()) {
            throw new DataExistException("Email đã tồn tại");
        }
        try {
            User user = userMapper.toCreateUser(request);
            user.setRole(buildRole(request.getRoleId()));
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setBlock(false);
            user.setDepartment("NO_DEPARTMENT");
            user.setPosition("INTERNSHIP");
            return userMapper.toUserDto(userRepository.saveAndFlush(user));
        }catch (Exception e){
            throw new MyCustomException("Có lỗi xảy ra trong quá trình thêm người dùng");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "User", description = "Cập nhật người dùng")
    public UserDto updateUser(UpdateUserRequest request) {
        Optional<User> userOptional = userRepository.findById(request.getId());
        if (!userOptional.isPresent()) {
            throw new DataExistException("Người dùng không tồn tại");
        }

        try {
            User user = userMapper.toUpdateUser(request);
            user.setRole(buildRole(request.getRoleId()));
            user.setPassword(userOptional.get().getPassword());
            return userMapper.toUserDto(userRepository.saveAndFlush(user));
        }catch (Exception e){
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhât người dùng");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "User", description = "Xóa người dùng")
    public void deleteUser(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            throw new DataExistException("Người dùng không tồn tại");
        }
        try {
            userRepository.deleteById(id);
        }catch (Exception e){
            throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa người dùng");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "User", description = "Xóa danh sách người dùng")
    public List<UserDto> deleteAllIdUsers(List<Long> ids) {
        List<UserDto> userDtos = new ArrayList<>();
        for (Long id : ids) {
            Optional<User> optionalNews = userRepository.findById(id);
            if (optionalNews.isPresent()) {
                User user = optionalNews.get();
                userDtos.add(userMapper.toUserDto(user));
                userRepository.delete(user);
            } else {
                throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa danh sách người dùng!");
            }
        }
        return userDtos;
    }

    private Role buildRole(String roleId) {
        return roleRepository.findById(roleId).orElseThrow(() -> new MyCustomException("Role không tồn tại!"));
    }
}