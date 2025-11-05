package ext.vnua.veterinary_beapp.modules.users.services.impl;

import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.users.model.PasswordHistory;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import ext.vnua.veterinary_beapp.modules.users.repository.PasswordHistoryRepository;
import ext.vnua.veterinary_beapp.modules.users.repository.UserRepository;
import ext.vnua.veterinary_beapp.modules.users.services.PasswordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {
    
    @Value("${password.history-check-count:3}")
    private int passwordHistoryCheckCount;
    
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Password policy patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[@$!%*?&].*");
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 50;
    
    @Override
    public void validatePasswordStrength(String password) {
        if (password == null || password.isBlank()) {
            throw new MyCustomException("Mật khẩu không được để trống");
        }
        
        if (password.length() < MIN_LENGTH) {
            throw new MyCustomException("Mật khẩu phải có ít nhất " + MIN_LENGTH + " ký tự");
        }
        
        if (password.length() > MAX_LENGTH) {
            throw new MyCustomException("Mật khẩu không được vượt quá " + MAX_LENGTH + " ký tự");
        }
        
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            throw new MyCustomException("Mật khẩu phải chứa ít nhất 1 chữ hoa");
        }
        
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            throw new MyCustomException("Mật khẩu phải chứa ít nhất 1 chữ thường");
        }
        
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            throw new MyCustomException("Mật khẩu phải chứa ít nhất 1 chữ số");
        }
        
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            throw new MyCustomException("Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt (@$!%*?&)");
        }
    }
    
    @Override
    public boolean isPasswordRecentlyUsed(User user, String rawPassword) {
        List<PasswordHistory> recentPasswords = passwordHistoryRepository
                .findTopNByUser(user, passwordHistoryCheckCount);
        
        for (PasswordHistory history : recentPasswords) {
            if (passwordEncoder.matches(rawPassword, history.getPasswordHash())) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    @Transactional
    public void savePasswordHistory(User user, String encodedPassword) {
        PasswordHistory history = PasswordHistory.builder()
                .user(user)
                .passwordHash(encodedPassword)
                .build();
        
        passwordHistoryRepository.save(history);
    }
    
    @Override
    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new MyCustomException("Mật khẩu hiện tại không đúng");
        }
        
        // Validate new password strength
        validatePasswordStrength(newPassword);
        
        // Check if password was recently used
        if (isPasswordRecentlyUsed(user, newPassword)) {
            throw new MyCustomException("Mật khẩu mới không được trùng với " + passwordHistoryCheckCount + " mật khẩu gần nhất");
        }
        
        // Save old password to history
        savePasswordHistory(user, user.getPassword());
        
        // Update password
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setPasswordChangedAt(Instant.now());
        user.setMustChangePassword(false);
        
        userRepository.save(user);
    }
}
