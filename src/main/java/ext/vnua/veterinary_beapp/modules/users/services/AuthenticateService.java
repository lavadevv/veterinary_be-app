package ext.vnua.veterinary_beapp.modules.users.services;

import ext.vnua.veterinary_beapp.modules.users.dto.entity.UserDto;
import ext.vnua.veterinary_beapp.modules.users.dto.request.*;
import ext.vnua.veterinary_beapp.modules.users.dto.response.ActiveSessionResponse;
import ext.vnua.veterinary_beapp.modules.users.dto.response.LoginResponse;
import ext.vnua.veterinary_beapp.modules.users.dto.response.RegisterResponse;
import ext.vnua.veterinary_beapp.modules.users.dto.response.TokenResponse;

import java.util.List;

public interface AuthenticateService {
    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);
    
    TokenResponse refreshToken(String refreshToken, String ipAddress, String userAgent);
    
    void logout(String refreshToken);
    
    void logoutAllDevices(String email);
    
    List<ActiveSessionResponse> getActiveSessions(String email, String currentToken);

    RegisterResponse register(RegisterRequest request);
    
    String verifyAccount(VerifyAccountRequest request);

    String regenerateOTP(RegenerateOtpRequest request);

    String forgotPassword(ForgotPasswordRequest request);

    UserDto updateProfile(UpdateProfileRequest request);
    
    void changePassword(String email, ChangePasswordRequest request);
}
