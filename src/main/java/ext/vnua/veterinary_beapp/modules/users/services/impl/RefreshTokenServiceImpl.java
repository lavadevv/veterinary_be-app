package ext.vnua.veterinary_beapp.modules.users.services.impl;

import ext.vnua.veterinary_beapp.exception.AuthenticateException;
import ext.vnua.veterinary_beapp.modules.users.dto.response.ActiveSessionResponse;
import ext.vnua.veterinary_beapp.modules.users.model.RefreshToken;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import ext.vnua.veterinary_beapp.modules.users.repository.RefreshTokenRepository;
import ext.vnua.veterinary_beapp.modules.users.services.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    
    @Value("${jwt.refresh-token-expiration:604800}") // 7 days default
    private Long refreshTokenDurationSeconds;
    
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user, String ipAddress, String userAgent) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(refreshTokenDurationSeconds))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .revoked(false)
                .build();
        
        return refreshTokenRepository.save(refreshToken);
    }
    
    @Override
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthenticateException("Refresh token không hợp lệ"));
        
        if (refreshToken.getRevoked()) {
            throw new AuthenticateException("Refresh token đã bị thu hồi");
        }
        
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthenticateException("Refresh token đã hết hạn");
        }
        
        return refreshToken;
    }
    
    @Override
    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthenticateException("Refresh token không tồn tại"));
        
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);
    }
    
    @Override
    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user, Instant.now());
    }
    
    @Override
    public List<ActiveSessionResponse> getActiveSessions(User user) {
        List<RefreshToken> activeTokens = refreshTokenRepository
                .findActiveTokensByUser(user, Instant.now());
        
        return activeTokens.stream()
                .map(token -> ActiveSessionResponse.builder()
                        .id(token.getId())
                        .ipAddress(token.getIpAddress())
                        .userAgent(token.getUserAgent())
                        .createdAt(token.getCreatedAt())
                        .expiryDate(token.getExpiryDate())
                        .current(false) // Will be set by controller
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void revokeSession(User user, Long sessionId) {
        RefreshToken token = refreshTokenRepository.findById(sessionId)
                .orElseThrow(() -> new AuthenticateException("Session không tồn tại"));
        
        if (!token.getUser().getId().equals(user.getId())) {
            throw new AuthenticateException("Không có quyền thu hồi session này");
        }
        
        token.setRevoked(true);
        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);
    }
    
    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM every day
    public void cleanupExpiredTokens() {
        int deleted = refreshTokenRepository.deleteExpiredTokens(Instant.now());
        System.out.println("Cleaned up " + deleted + " expired refresh tokens");
    }
}
