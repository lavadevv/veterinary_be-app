package ext.vnua.veterinary_beapp.modules.users.dto.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

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

    private String department;

    private String position;

    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", block=" + block +
                ", role=" + (role != null ? role.getName() : "null") +
                ", department='" + department + '\'' +
                ", position='" + position + '\'' +
                '}';
    }

}
