package ext.vnua.veterinary_beapp.modules.users.services.impl;

import ext.vnua.veterinary_beapp.modules.users.enums.UserStatus;
import ext.vnua.veterinary_beapp.modules.users.model.LoginAttempt;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import ext.vnua.veterinary_beapp.modules.users.repository.LoginAttemptRepository;
import ext.vnua.veterinary_beapp.modules.users.repository.UserRepository;
import ext.vnua.veterinary_beapp.modules.users.services.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {
    
    @Value("${security.max-login-attempts:5}")
    private int maxLoginAttempts;
    
    @Value("${security.login-attempt-window-minutes:15}")
    private int attemptWindowMinutes;
    
    @Value("${security.account-lock-duration-minutes:30}")
    private int accountLockDurationMinutes;
    
    @Value("${security.max-ip-attempts:10}")
    private int maxIpAttempts;
    
    private final LoginAttemptRepository loginAttemptRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public void recordLoginAttempt(String email, boolean successful, String ipAddress, 
                                   String userAgent, String failureReason) {
        LoginAttempt attempt = LoginAttempt.builder()
                .email(email)
                .successful(successful)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .failureReason(failureReason)
                .build();
        
        loginAttemptRepository.save(attempt);
        
        // Update user's failed login attempts counter
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (successful) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
            } else {
                int failedAttempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
                user.setFailedLoginAttempts(failedAttempts);
                
                // Lock account if exceeded max attempts
                if (failedAttempts >= maxLoginAttempts) {
                    user.setLockedUntil(Instant.now().plus(accountLockDurationMinutes, ChronoUnit.MINUTES));
                    user.setStatus(UserStatus.LOCKED.name());
                }
            }
            userRepository.save(user);
        }
    }
    
    @Override
    public boolean isAccountLocked(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Check if account is locked and lock hasn't expired
        if (user.getLockedUntil() != null && Instant.now().isBefore(user.getLockedUntil())) {
            return true;
        }
        
        // If lock has expired, unlock the account
        if (user.getLockedUntil() != null && Instant.now().isAfter(user.getLockedUntil())) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            user.setStatus(UserStatus.ACTIVE.name());
            userRepository.save(user);
        }
        
        return false;
    }
    
    @Override
    public boolean isIpBlocked(String ipAddress) {
        Instant since = Instant.now().minus(attemptWindowMinutes, ChronoUnit.MINUTES);
        long failedCount = loginAttemptRepository.countFailedAttemptsByIpSince(ipAddress, since);
        return failedCount >= maxIpAttempts;
    }
    
    @Override
    public long getFailedAttemptsCount(String email) {
        Instant since = Instant.now().minus(attemptWindowMinutes, ChronoUnit.MINUTES);
        return loginAttemptRepository.countFailedAttemptsSince(email, since);
    }
    
    @Override
    @Transactional
    public void resetFailedAttempts(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }
}
