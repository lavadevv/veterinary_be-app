package ext.vnua.veterinary_beapp.modules.users.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, unique = true, length = 500)
    private String token;
    
    @Column(nullable = false)
    private Instant expiryDate;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column
    private String ipAddress;
    
    @Column
    private String userAgent;
    
    @Column(nullable = false)
    private Boolean revoked = false;
    
    @Column
    private Instant revokedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
    
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }
}
