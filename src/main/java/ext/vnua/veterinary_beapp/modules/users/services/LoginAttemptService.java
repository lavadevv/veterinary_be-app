package ext.vnua.veterinary_beapp.modules.users.services;

public interface LoginAttemptService {
    
    /**
     * Record login attempt
     */
    void recordLoginAttempt(String email, boolean successful, String ipAddress, 
                           String userAgent, String failureReason);
    
    /**
     * Check if account is locked due to failed attempts
     */
    boolean isAccountLocked(String email);
    
    /**
     * Check if IP is blocked due to too many failed attempts
     */
    boolean isIpBlocked(String ipAddress);
    
    /**
     * Get failed attempts count in last 15 minutes
     */
    long getFailedAttemptsCount(String email);
    
    /**
     * Reset failed attempts after successful login
     */
    void resetFailedAttempts(String email);
}
