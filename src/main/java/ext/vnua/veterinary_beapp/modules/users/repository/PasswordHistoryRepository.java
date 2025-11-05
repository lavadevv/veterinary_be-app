package ext.vnua.veterinary_beapp.modules.users.repository;

import ext.vnua.veterinary_beapp.modules.users.model.PasswordHistory;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    
    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.user = ?1 ORDER BY ph.createdAt DESC")
    List<PasswordHistory> findByUserOrderByCreatedAtDesc(User user);
    
    @Query(value = "SELECT ph FROM PasswordHistory ph WHERE ph.user = ?1 ORDER BY ph.createdAt DESC LIMIT ?2")
    List<PasswordHistory> findTopNByUser(User user, int limit);
}
