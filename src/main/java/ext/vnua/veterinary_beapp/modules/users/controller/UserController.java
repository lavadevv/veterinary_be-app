package ext.vnua.veterinary_beapp.modules.users.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.users.dto.entity.UserDto;
import ext.vnua.veterinary_beapp.modules.users.dto.request.CreateUserRequest;
import ext.vnua.veterinary_beapp.modules.users.dto.request.GetUserRequest;
import ext.vnua.veterinary_beapp.modules.users.dto.request.UpdateUserRequest;
import ext.vnua.veterinary_beapp.modules.users.mapper.UserMapper;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import ext.vnua.veterinary_beapp.modules.users.services.UserService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    private final UserMapper userMapper;

    @ApiOperation(value = "Lấy thông tin nhiêu tài khoản")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN' , 'EMPLOYEE')")
    public ResponseEntity<?> getAllUser(@Valid @ModelAttribute GetUserRequest request) {
        Page<User> page = userService.getAllUser(request, PageRequest.of(request.getStart(), request.getLimit()));

        return BaseResponse.successListData(page.getContent().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList()), (int) page.getTotalElements());
    }

    @ApiOperation(value = "Lấy avatar của một tài khoản")
    @GetMapping("/{id}/avatar")
    //@PreAuthorize("#oauth2.hasAnyScope('read')") // for authenticated request (logged)
    public ResponseEntity<?> getAvatar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(new AbstractMap.SimpleEntry<>("data", userService.selectUserById(id).getB64()));
    }

    @ApiOperation(value = "Lấy thông tin của một tài khoản")
    @GetMapping("/{id}")

    public ResponseEntity<?> getUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.selectUserById(id));
    }

    @ApiOperation(value = "Tạo một người dùng")
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN' , 'EMPLOYEE')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @ApiOperation(value = "Cập nhật một người dùng")
    @PutMapping("")

    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(request));
    }

    @ApiOperation(value = "Xóa một người dùng")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Xóa người dùng thành công");
    }

    @ApiOperation(value = "Xóa nhiều người dùng")
    @DeleteMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    ResponseEntity<?> deleteAllById(@RequestBody List<Long> ids) {
        List<UserDto> response = userService.deleteAllIdUsers(ids);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Upload avatar một tài khoản")
    @PostMapping("/{id}/avatar")
//    @PreAuthorize("#oauth2.hasAnyScope('read')") // for authenticated request (logged)
    public ResponseEntity<?> uploadAvatar(@PathVariable("id") Long id,
                                          @RequestParam("avatar") MultipartFile file) throws IOException {
//        String token = header.substring(7);
//        String email = jwtConfig.getUserIdFromJWT(token);

        UserDto userDto = userService.selectUserById(id);

        userService.changeAvatar(userDto.getEmail(), file.getBytes());
        return ResponseEntity.ok(getAvatar(id));
    }
}
