package ext.vnua.veterinary_beapp.modules.users.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "login_attempts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private Boolean successful;
    
    @Column
    private String ipAddress;
    
    @Column
    private String userAgent;
    
    @Column
    private String failureReason;
    
    @Column(nullable = false)
    private Instant attemptTime;
    
    @PrePersist
    protected void onCreate() {
        attemptTime = Instant.now();
    }
}
