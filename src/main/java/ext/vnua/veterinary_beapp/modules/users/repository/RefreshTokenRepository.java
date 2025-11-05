package ext.vnua.veterinary_beapp.modules.users.repository;

import ext.vnua.veterinary_beapp.modules.users.model.RefreshToken;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    Optional<RefreshToken> findByToken(String token);
    
    List<RefreshToken> findByUserAndRevokedFalseAndExpiryDateAfter(User user, Instant now);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = ?1 AND rt.revoked = false AND rt.expiryDate > ?2")
    List<RefreshToken> findActiveTokensByUser(User user, Instant now);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = ?2 WHERE rt.user = ?1 AND rt.revoked = false")
    int revokeAllUserTokens(User user, Instant revokedAt);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < ?1")
    int deleteExpiredTokens(Instant now);
}
