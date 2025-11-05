package ext.vnua.veterinary_beapp.modules.users.dto.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;

    private String fullName;

    private String email;

    private String address;

    private String phone;

    private boolean block;

    private String b64;

    private String fileType;

    private RoleDto role;

    // chỉ trả về ID để FE tự resolve name từ org store
    private Long departmentId;
    private Long positionId;

    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;

    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", block=" + block +
                ", role=" + (role != null ? role.getName() : "null") +
                ", departmentId=" + departmentId +
                ", positionId=" + positionId +
                '}';
    }
}
