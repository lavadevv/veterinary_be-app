package ext.vnua.veterinary_beapp.modules.users.controller;

import ext.vnua.veterinary_beapp.config.JwtConfig;
import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.users.dto.entity.UserDto;
import ext.vnua.veterinary_beapp.modules.users.dto.request.*;
import ext.vnua.veterinary_beapp.modules.users.dto.response.LoginResponse;
import ext.vnua.veterinary_beapp.modules.users.dto.response.RegisterResponse;
import ext.vnua.veterinary_beapp.modules.users.services.AuthenticateService;
import ext.vnua.veterinary_beapp.modules.users.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticateService authenticateService;
    private final UserService userService;
    private final JwtConfig jwtConfig;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){
        LoginResponse response=authenticateService.login(request);
        return BaseResponse.successData(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){
        RegisterResponse response=authenticateService.register(request);
        return BaseResponse.successData(response);
    }

    @PutMapping("verify-account")
    public ResponseEntity<?> verifyAccount(@Valid @RequestBody VerifyAccountRequest request) {
        return BaseResponse.successData(authenticateService.verifyAccount(request));
    }

    @PutMapping("regenerate-otp")
    public ResponseEntity<?> regenerateOTP(@Valid @RequestBody RegenerateOtpRequest request) {
        return BaseResponse.successData(authenticateService.regenerateOTP(request));
    }

    @PutMapping("forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return BaseResponse.successData(authenticateService.forgotPassword(request));
    }

    @PutMapping("profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return BaseResponse.successData(authenticateService.updateProfile(request));
    }

    @GetMapping("profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String email=jwtConfig.getUserIdFromJWT(jwt);
        UserDto userDto=userService.selectUserByEmail(email);
        return BaseResponse.successData(userDto);
    }
}
