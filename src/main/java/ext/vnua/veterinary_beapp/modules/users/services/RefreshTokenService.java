package ext.vnua.veterinary_beapp.modules.users.services;

import ext.vnua.veterinary_beapp.modules.users.dto.response.ActiveSessionResponse;
import ext.vnua.veterinary_beapp.modules.users.model.RefreshToken;
import ext.vnua.veterinary_beapp.modules.users.model.User;

import java.util.List;

public interface RefreshTokenService {
    
    /**
     * Create new refresh token for user
     */
    RefreshToken createRefreshToken(User user, String ipAddress, String userAgent);
    
    /**
     * Verify and get refresh token
     */
    RefreshToken verifyRefreshToken(String token);
    
    /**
     * Revoke single refresh token
     */
    void revokeToken(String token);
    
    /**
     * Revoke all refresh tokens for a user
     */
    void revokeAllUserTokens(User user);
    
    /**
     * Get all active sessions for a user
     */
    List<ActiveSessionResponse> getActiveSessions(User user);
    
    /**
     * Revoke specific session
     */
    void revokeSession(User user, Long sessionId);
    
    /**
     * Clean up expired tokens (scheduled task)
     */
    void cleanupExpiredTokens();
}
