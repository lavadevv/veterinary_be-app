package ext.vnua.veterinary_beapp.modules.users.services.impl;

import ext.vnua.veterinary_beapp.common.MailUtil;
import ext.vnua.veterinary_beapp.common.OtpUtil;
import ext.vnua.veterinary_beapp.common.RoleEnum;
import ext.vnua.veterinary_beapp.config.JwtConfig;
import ext.vnua.veterinary_beapp.dto.custom.CustomUserDetails;
import ext.vnua.veterinary_beapp.exception.AuthenticateException;
import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.DataNotFoundException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.users.dto.entity.UserDto;
import ext.vnua.veterinary_beapp.modules.users.dto.request.*;
import ext.vnua.veterinary_beapp.modules.users.dto.response.ActiveSessionResponse;
import ext.vnua.veterinary_beapp.modules.users.dto.response.LoginResponse;
import ext.vnua.veterinary_beapp.modules.users.dto.response.RegisterResponse;
import ext.vnua.veterinary_beapp.modules.users.dto.response.TokenResponse;
import ext.vnua.veterinary_beapp.modules.users.enums.UserStatus;
import ext.vnua.veterinary_beapp.modules.users.mapper.UserMapper;
import ext.vnua.veterinary_beapp.modules.users.model.Department;
import ext.vnua.veterinary_beapp.modules.users.model.Position;
import ext.vnua.veterinary_beapp.modules.users.model.RefreshToken;
import ext.vnua.veterinary_beapp.modules.users.model.Role;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import ext.vnua.veterinary_beapp.modules.users.repository.DepartmentRepository;
import ext.vnua.veterinary_beapp.modules.users.repository.PositionRepository;
import ext.vnua.veterinary_beapp.modules.users.repository.RoleRepository;
import ext.vnua.veterinary_beapp.modules.users.repository.UserRepository;
import ext.vnua.veterinary_beapp.modules.users.services.AuthenticateService;
import ext.vnua.veterinary_beapp.modules.users.services.LoginAttemptService;
import ext.vnua.veterinary_beapp.modules.users.services.PasswordService;
import ext.vnua.veterinary_beapp.modules.users.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticateServiceImp implements AuthenticateService {

    private static final long OTP_VERIFY_TTL_SECONDS = 600; // 10 phút (tăng từ 2 phút)
    private static final long OTP_RESET_TTL_SECONDS  = 600;  // 10 phút (tăng từ 1 phút)
    private static final long OTP_REGEN_MIN_INTERVAL_SECONDS = 30; // chống spam gửi OTP
    
    @Value("${jwt.expiration:900}") // 15 minutes default
    private Long accessTokenExpiration;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;
    private final AuthenticationManager authenticationManager;
    private final MailUtil mailUtil;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordService passwordService;

    @Override
    @Auditable(action = AuditAction.LOGIN, entityName = "User", description = "Đăng nhập hệ thống")
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        // Check if IP is blocked
        if (loginAttemptService.isIpBlocked(ipAddress)) {
            throw new AuthenticateException("IP của bạn đã bị khóa do quá nhiều lần đăng nhập thất bại. Vui lòng thử lại sau 15 phút.");
        }
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    loginAttemptService.recordLoginAttempt(request.getEmail(), false, ipAddress, userAgent, "Email không tồn tại");
                    return new AuthenticateException("Email hoặc mật khẩu không đúng");
                });
        
        // Check if account is locked
        if (loginAttemptService.isAccountLocked(request.getEmail())) {
            long remainingMinutes = Duration.between(Instant.now(), user.getLockedUntil()).toMinutes();
            throw new AuthenticateException("Tài khoản đã bị khóa do quá nhiều lần đăng nhập thất bại. Vui lòng thử lại sau " + remainingMinutes + " phút.");
        }
        
        // Check account status
        if (UserStatus.LOCKED.name().equals(user.getStatus())) {
            loginAttemptService.recordLoginAttempt(request.getEmail(), false, ipAddress, userAgent, "Tài khoản bị khóa");
            throw new AuthenticateException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }
        
        if (UserStatus.INACTIVE.name().equals(user.getStatus())) {
            loginAttemptService.recordLoginAttempt(request.getEmail(), false, ipAddress, userAgent, "Tài khoản không hoạt động");
            throw new AuthenticateException("Tài khoản đã bị vô hiệu hóa.");
        }
        
        if (UserStatus.PENDING_VERIFICATION.name().equals(user.getStatus())) {
            loginAttemptService.recordLoginAttempt(request.getEmail(), false, ipAddress, userAgent, "Tài khoản chưa xác thực");
            throw new AuthenticateException("Tài khoản chưa được xác thực. Vui lòng kiểm tra email để xác thực.");
        }
        
        // Deprecated: Old block field check (backward compatibility)
        if (Boolean.TRUE.equals(user.getBlock())) {
            loginAttemptService.recordLoginAttempt(request.getEmail(), false, ipAddress, userAgent, "Tài khoản bị khóa (legacy)");
            throw new AuthenticateException("Tài khoản bị khóa");
        }

        try {
            // Ủy quyền xác thực cho AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Record successful login
            loginAttemptService.recordLoginAttempt(request.getEmail(), true, ipAddress, userAgent, null);
            
            // Generate tokens
            String accessToken = jwtConfig.generateToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, ipAddress, userAgent);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return LoginResponse.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(accessTokenExpiration)
                    .roles(roles)
                    .status(user.getStatus())
                    .mustChangePassword(user.getMustChangePassword())
                    .build();
        } catch (Exception e) {
            loginAttemptService.recordLoginAttempt(request.getEmail(), false, ipAddress, userAgent, "Mật khẩu không đúng");
            throw new AuthenticateException("Email hoặc mật khẩu không đúng");
        }
    }
    
    @Override
    public TokenResponse refreshToken(String refreshTokenStr, String ipAddress, String userAgent) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenStr);
        User user = refreshToken.getUser();
        
        // Check if user is still active
        if (!UserStatus.ACTIVE.name().equals(user.getStatus())) {
            throw new AuthenticateException("Tài khoản không còn hoạt động");
        }
        
        // Generate new access token
        String newAccessToken = jwtConfig.generateToken(user);
        
        // Optionally: rotate refresh token (more secure)
        refreshTokenService.revokeToken(refreshTokenStr);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user, ipAddress, userAgent);
        
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration)
                .build();
    }
    
    @Override
    @Auditable(action = AuditAction.LOGOUT, entityName = "User", description = "Đăng xuất")
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }
    
    @Override
    @Auditable(action = AuditAction.LOGOUT, entityName = "User", description = "Đăng xuất tất cả thiết bị")
    public void logoutAllDevices(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Người dùng không tồn tại"));
        refreshTokenService.revokeAllUserTokens(user);
    }
    
    @Override
    public List<ActiveSessionResponse> getActiveSessions(String email, String currentToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Người dùng không tồn tại"));
        
        List<ActiveSessionResponse> sessions = refreshTokenService.getActiveSessions(user);
        
        // Mark current session if currentToken is provided
        if (currentToken != null && !currentToken.isEmpty()) {
            try {
                RefreshToken token = refreshTokenService.verifyRefreshToken(currentToken);
                sessions.forEach(session -> {
                    if (session.getId().equals(token.getId())) {
                        session.setCurrent(true);
                    }
                });
            } catch (Exception e) {
                // If token verification fails, just don't mark any session as current
                // This is not critical - user can still see all sessions
            }
        }
        
        return sessions;
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DataExistException("Email đã tồn tại");
        }
        
        // Validate password strength
        passwordService.validatePasswordStrength(request.getPassword());

        // Lấy role VIEWER hoặc khởi tạo nếu chưa có
        Role role = roleRepository.findById(RoleEnum.VIEWER.name())
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleId(RoleEnum.VIEWER.name());
                    r.setName("Người Xem");
                    r.setDescription("Chả có quyền gì cả. Chill chill thôi");
                    return roleRepository.saveAndFlush(r);
                });

        User user = new User();
        user.setAddress(request.getAddress());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setIsSuperAdmin(false);
        user.setBlock(true); // khóa cho đến khi xác thực OTP (backward compatibility)
        user.setStatus(UserStatus.PENDING_VERIFICATION.name()); // New status field
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPasswordChangedAt(Instant.now());
        user.setRole(role);

        // Khởi tạo OTP
        String otp = OtpUtil.generateOtp();
        user.setOtp(otp);
        user.setOtpGeneratedTime(Instant.now());

        userRepository.saveAndFlush(user);

        try {
            mailUtil.sendOtpEmail(user.getEmail(), otp);
        } catch (Exception ex) {
            // rollback mềm: xoá OTP để không treo trạng thái
            user.setOtp(null);
            user.setOtpGeneratedTime(null);
            userRepository.save(user);
            throw new MyCustomException("Gửi email OTP thất bại, vui lòng thử lại sau");
        }

        return RegisterResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .address(user.getAddress())
                .phone(user.getPhone())
                .build();
    }

    @Override
    public String verifyAccount(VerifyAccountRequest request) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Không tồn tại người dùng có email là: " + email));

        if (user.getOtp() == null || user.getOtpGeneratedTime() == null) {
            throw new MyCustomException("OTP không hợp lệ, vui lòng tạo lại.");
        }

        boolean otpValid = user.getOtp().equals(request.getOtp());
        boolean withinTtl = Duration.between(user.getOtpGeneratedTime(), Instant.now()).getSeconds() < OTP_VERIFY_TTL_SECONDS;

        if (otpValid && withinTtl) {
            user.setBlock(false);
            user.setStatus(UserStatus.ACTIVE.name()); // Activate account
            // Xoá OTP sau khi xác thực thành công để tránh reuse
            user.setOtp(null);
            user.setOtpGeneratedTime(null);
            userRepository.save(user);
            return "OTP đã xác thực thành công. Tài khoản đã được kích hoạt.";
        } else {
            throw new MyCustomException("OTP không đúng hoặc đã hết hạn. Hãy tạo lại OTP và thử lại.");
        }
    }

    @Override
    public String regenerateOTP(RegenerateOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new DataNotFoundException("Không tồn tại người dùng có email là: " + request.getEmail()));

        // Chống spam: yêu cầu tối thiểu 30s giữa 2 lần gửi
        if (user.getOtpGeneratedTime() != null) {
            long elapsed = Duration.between(user.getOtpGeneratedTime(), Instant.now()).getSeconds();
            if (elapsed < OTP_REGEN_MIN_INTERVAL_SECONDS) {
                long wait = OTP_REGEN_MIN_INTERVAL_SECONDS - elapsed;
                throw new MyCustomException("Vui lòng đợi " + wait + " giây trước khi yêu cầu OTP mới.");
            }
        }

        String otp = OtpUtil.generateOtp();
        user.setOtp(otp);
        user.setOtpGeneratedTime(Instant.now());
        userRepository.save(user);

        try {
            mailUtil.sendOtpEmail(request.getEmail(), otp);
        } catch (Exception ex) {
            throw new MyCustomException("Gửi email OTP thất bại, vui lòng thử lại sau");
        }

        return "Email đã gửi... Hãy xác thực trong 2 phút";
    }

    @Override
    public String forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Không tồn tại người dùng có email là: " + email));

        if (user.getOtp() == null || user.getOtpGeneratedTime() == null) {
            throw new MyCustomException("OTP không hợp lệ, vui lòng tạo lại.");
        }

        boolean otpValid = user.getOtp().equals(request.getOtp());
        boolean withinTtl = Duration.between(user.getOtpGeneratedTime(), Instant.now()).getSeconds() < OTP_RESET_TTL_SECONDS;

        if (otpValid && withinTtl) {
            // Validate new password strength
            passwordService.validatePasswordStrength(request.getPassword());
            
            // Change password (this will check history and update passwordChangedAt)
            // Pass null for currentPassword since this is password reset, not change
            passwordService.changePassword(user, null, request.getPassword());
            
            // Xoá OTP sau khi reset password thành công
            user.setOtp(null);
            user.setOtpGeneratedTime(null);
            
            // Clear mustChangePassword flag if set
            user.setMustChangePassword(false);
            
            // Revoke all refresh tokens for security
            refreshTokenService.revokeAllUserTokens(user);
            
            userRepository.save(user);
            return "Mật khẩu đã được thay đổi thành công. Vui lòng đăng nhập lại.";
        } else {
            throw new MyCustomException("OTP không đúng hoặc đã hết hạn. Hãy tạo lại OTP và thử lại.");
        }
    }

    @Override
    @Auditable(action = AuditAction.UPDATE, entityName = "User", description = "Người dùng tự sửa thông tin")
    public UserDto updateProfile(UpdateProfileRequest request) {
        // Ưu tiên lấy từ SecurityContext để tránh sửa hồ sơ người khác
        String principalEmail = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            principalEmail = cud.getUsername();
        }

        String emailToUpdate = request.getEmail();
        if (principalEmail != null && emailToUpdate != null && !principalEmail.equalsIgnoreCase(emailToUpdate)) {
            throw new AuthenticateException("Bạn không thể sửa thông tin của người dùng khác.");
        }

        String email = (emailToUpdate != null) ? emailToUpdate : principalEmail;
        if (email == null) {
            throw new AuthenticateException("Không xác định được tài khoản để cập nhật.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataExistException("Người dùng không tồn tại"));

        try {
            user.setFullName(request.getFullName());
            user.setPhone(request.getPhone());
            user.setAddress(request.getAddress());
            return userMapper.toUserDto(userRepository.saveAndFlush(user));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhật người dùng");
        }
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Không tồn tại người dùng có email là: " + email));

        // Verify old password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AuthenticateException("Mật khẩu cũ không chính xác");
        }

        // Validate new password and change it (will check history)
        passwordService.changePassword(user, request.getCurrentPassword(), request.getNewPassword());

        // Clear mustChangePassword flag if set
        user.setMustChangePassword(false);

        userRepository.save(user);

        // Optionally revoke all refresh tokens to force re-login on all devices
        // refreshTokenService.revokeAllUserTokens(user);
    }
}
