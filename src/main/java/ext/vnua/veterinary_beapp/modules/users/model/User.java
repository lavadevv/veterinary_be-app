package ext.vnua.veterinary_beapp.modules.users.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address")
    private String address;

    @Column(name = "block")
    private Boolean block;

    @Column(name = "password")
    private String password;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "is_super_admin")
    private Boolean isSuperAdmin = false;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Lob
    @Column(name = "b64")
    private String b64;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "otp")
    private String otp;

    @Column(name = "otp_generated_time")
    private Instant otpGeneratedTime;
    
    // Soft delete
    @Column(name = "deleted_at")
    private Instant deletedAt;
    
    @Column(name = "deleted_by")
    private String deletedBy;
    
    // User status: ACTIVE, INACTIVE, LOCKED, PENDING_VERIFICATION
    @Column(name = "status", length = 30)
    private String status = "PENDING_VERIFICATION";
    
    // Password management
    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;
    
    @Column(name = "must_change_password")
    private Boolean mustChangePassword = false;
    
    // Account locking
    @Column(name = "locked_until")
    private Instant lockedUntil;
    
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_users_department"))
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", foreignKey = @ForeignKey(name = "fk_users_position"))
    private Position position;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (role != null) {
            role.getPermissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.getPermissionId()))); // lay den quyen
            authorities.add(new SimpleGrantedAuthority(role.getRoleId())); //lay den vai tro
        }
        return authorities;
    }
}
