package ext.vnua.veterinary_beapp.modules.users.services;

import ext.vnua.veterinary_beapp.modules.users.model.User;

public interface PasswordService {
    
    /**
     * Validate password strength
     */
    void validatePasswordStrength(String password);
    
    /**
     * Check if password was used recently
     */
    boolean isPasswordRecentlyUsed(User user, String rawPassword);
    
    /**
     * Save password to history
     */
    void savePasswordHistory(User user, String encodedPassword);
    
    /**
     * Change user password
     */
    void changePassword(User user, String currentPassword, String newPassword);
}
