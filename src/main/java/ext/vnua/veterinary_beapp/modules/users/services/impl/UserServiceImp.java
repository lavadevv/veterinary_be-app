package ext.vnua.veterinary_beapp.modules.users.services.impl;

import ext.vnua.veterinary_beapp.dto.custom.CustomUserDetails;
import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.audits.service.AuditService;
import ext.vnua.veterinary_beapp.modules.users.dto.entity.UserDto;
import ext.vnua.veterinary_beapp.modules.users.dto.request.CreateUserRequest;
import ext.vnua.veterinary_beapp.modules.users.dto.request.UpdateUserRequest;
import ext.vnua.veterinary_beapp.modules.users.enums.UserStatus;
import ext.vnua.veterinary_beapp.modules.users.mapper.UserMapper;
import ext.vnua.veterinary_beapp.modules.users.model.Department;
import ext.vnua.veterinary_beapp.modules.users.model.Position;
import ext.vnua.veterinary_beapp.modules.users.model.Role;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import ext.vnua.veterinary_beapp.modules.users.repository.*;
import ext.vnua.veterinary_beapp.modules.users.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    @Value("${DEFAULT-PASSWORD}")
    private String defaultPassword;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS) // readOnly tương đương trong jakarta tx
    public Page<User> getAllUser(CustomUserQuery.UserFilterParam param, PageRequest pageRequest) {
        Specification<User> specification = CustomUserQuery.getFilterUser(param);
        return userRepository.findAll(specification, pageRequest);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public UserDto selectUserByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            throw new DataExistException("Email không tồn tại");
        }
        User user = userOptional.get();
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public UserDto selectUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            throw new DataExistException("Người dùng không tồn tại");
        }
        User user = userOptional.get();
        return userMapper.toUserDto(user);
    }

    @Override
    public void changeAvatar(String email, byte[] fileBytes) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            throw new DataExistException("Email không tồn tại");
        }
        User user = userOptional.get();
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
            user.setBlock(false);

            Department dep = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new MyCustomException("Department không tồn tại"));
            Position pos = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new MyCustomException("Position không tồn tại"));
            user.setDepartment(dep);
            user.setPosition(pos);

            String rawPwd = (request.getPassword() == null || request.getPassword().isBlank())
                    ? defaultPassword
                    : request.getPassword();

            user.setPassword(passwordEncoder.encode(rawPwd));

            return userMapper.toUserDto(userRepository.saveAndFlush(user));
        } catch (Exception e) {
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
            User current = userOptional.get();
            User user = userMapper.toUpdateUser(request);
            user.setPassword(current.getPassword());
            user.setRole(buildRole(request.getRoleId()));

            Department dep = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new MyCustomException("Department không tồn tại"));
            Position pos = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new MyCustomException("Position không tồn tại"));
            user.setDepartment(dep);
            user.setPosition(pos);

            return userMapper.toUserDto(userRepository.saveAndFlush(user));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhât người dùng");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "User", description = "Xóa người dùng (soft delete)")
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Người dùng không tồn tại"));
        
        // Check if already deleted
        if (user.getDeletedAt() != null) {
            throw new MyCustomException("Người dùng đã bị xóa trước đó");
        }
        
        try {
            // Soft delete
            user.setDeletedAt(Instant.now());
            
            // Get current user from security context for audit
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
                user.setDeletedBy(cud.getUsername());
            }
            
            // Set status to INACTIVE
            user.setStatus(UserStatus.INACTIVE.name());
            
            userRepository.save(user);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa người dùng: " + e.getMessage());
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "User", description = "Xóa danh sách người dùng (soft delete)")
    public List<UserDto> deleteAllIdUsers(List<Long> ids) {
        List<UserDto> userDtos = new ArrayList<>();
        
        // Get current user for audit
        String deletedBy = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            deletedBy = cud.getUsername();
        }
        
        for (Long id : ids) {
            Optional<User> optionalUser = userRepository.findById(id);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                
                // Skip if already deleted
                if (user.getDeletedAt() != null) {
                    continue;
                }
                
                // Soft delete
                user.setDeletedAt(Instant.now());
                user.setDeletedBy(deletedBy);
                user.setStatus(UserStatus.INACTIVE.name());
                
                User savedUser = userRepository.save(user);
                userDtos.add(userMapper.toUserDto(savedUser));
            } else {
                throw new MyCustomException("Người dùng với ID " + id + " không tồn tại!");
            }
        }
        return userDtos;
    }

    private Role buildRole(String roleId) {
        return roleRepository.findById(roleId).orElseThrow(() -> new MyCustomException("Role không tồn tại!"));
    }
}
