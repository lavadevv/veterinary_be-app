package ext.vnua.veterinary_beapp.modules.users.repository;

import ext.vnua.veterinary_beapp.modules.users.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    
    @Query("SELECT la FROM LoginAttempt la WHERE la.email = ?1 AND la.attemptTime > ?2 ORDER BY la.attemptTime DESC")
    List<LoginAttempt> findRecentAttemptsByEmail(String email, Instant since);
    
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.email = ?1 AND la.successful = false AND la.attemptTime > ?2")
    long countFailedAttemptsSince(String email, Instant since);
    
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.ipAddress = ?1 AND la.successful = false AND la.attemptTime > ?2")
    long countFailedAttemptsByIpSince(String ipAddress, Instant since);
}
