package ext.vnua.veterinary_beapp.modules.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn; // seconds
    private Long id;
    private String name;
    private String fullName;
    private String phone;
    private String email;
    private List<String> roles;
    private String status;
    private Boolean mustChangePassword;
}
