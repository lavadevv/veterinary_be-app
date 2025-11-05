package ext.vnua.veterinary_beapp.modules.users.controller;

import ext.vnua.veterinary_beapp.dto.custom.CustomUserDetails;
import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.users.dto.entity.UserDto;
import ext.vnua.veterinary_beapp.modules.users.dto.request.*;
import ext.vnua.veterinary_beapp.modules.users.dto.response.ActiveSessionResponse;
import ext.vnua.veterinary_beapp.modules.users.dto.response.TokenResponse;
import ext.vnua.veterinary_beapp.modules.users.mapper.UserMapper;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import ext.vnua.veterinary_beapp.modules.users.services.AuthenticateService;
import ext.vnua.veterinary_beapp.modules.users.services.UserService;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final AuthenticateService authenticateService;
    private final UserMapper userMapper;

    @ApiOperation(value = "Lấy thông tin nhiêu tài khoản")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
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
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> getUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.selectUserById(id));
    }

    @ApiOperation(value = "Tạo một người dùng")
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
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

    // ==================== NEW SECURITY ENDPOINTS ====================

    @ApiOperation(value = "Đổi mật khẩu người dùng hiện tại")
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        authenticateService.changePassword(userDetails.getUsername(), request);
        return BaseResponse.successData("Đổi mật khẩu thành công");
    }

    @ApiOperation(value = "Refresh access token bằng refresh token")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        TokenResponse response = authenticateService.refreshToken(
                request.getRefreshToken(), 
                ipAddress, 
                userAgent
        );
        
        return BaseResponse.successData(response);
    }

    @ApiOperation(value = "Đăng xuất phiên hiện tại")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authenticateService.logout(request.getRefreshToken());
        return BaseResponse.successData("Đăng xuất thành công");
    }

    @ApiOperation(value = "Đăng xuất tất cả thiết bị")
    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logoutAllDevices(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authenticateService.logoutAllDevices(userDetails.getUsername());
        return BaseResponse.successData("Đã đăng xuất tất cả thiết bị");
    }

    @ApiOperation(value = "Lấy danh sách phiên đang hoạt động")
    @GetMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getActiveSessions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Extract refresh token from request or use a separate header
        String currentToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // In practice, you might want to pass refresh token in request body or separate header
            // This is just for demonstration
        }
        
        List<ActiveSessionResponse> sessions = authenticateService.getActiveSessions(
                userDetails.getUsername(), 
                currentToken
        );
        
        return ResponseEntity.ok(BaseResponse.successListData(sessions, sessions.size()));
    }

    @ApiOperation(value = "Xóa một phiên đăng nhập cụ thể")
    @DeleteMapping("/sessions/{refreshToken}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> revokeSession(@PathVariable("refreshToken") String refreshToken) {
        authenticateService.logout(refreshToken);
        return BaseResponse.successData("Đã xóa phiên đăng nhập");
    }

    // ==================== HELPER METHODS ====================

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
