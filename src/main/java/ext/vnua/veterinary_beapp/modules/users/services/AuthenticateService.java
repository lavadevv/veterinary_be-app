package ext.vnua.veterinary_beapp.modules.users.services;

import ext.vnua.veterinary_beapp.modules.users.dto.entity.UserDto;
import ext.vnua.veterinary_beapp.modules.users.dto.request.*;
import ext.vnua.veterinary_beapp.modules.users.dto.response.LoginResponse;
import ext.vnua.veterinary_beapp.modules.users.dto.response.RegisterResponse;

public interface AuthenticateService {
    LoginResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);
    String verifyAccount(VerifyAccountRequest request);

    String regenerateOTP(RegenerateOtpRequest request);

    String forgotPassword(ForgotPasswordRequest request);

    UserDto updateProfile(UpdateProfileRequest request);
}
