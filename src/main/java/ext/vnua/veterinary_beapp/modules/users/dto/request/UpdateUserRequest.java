package ext.vnua.veterinary_beapp.modules.users.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotNull(message = "Id không được để trống")
    private Long id;
    @Email( regexp = ".+[@].+[\\.].+",message = "Email không hợp lệ")
    private String email;
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;
    @NotBlank(message = "Phòng ban không được để trống")
    private String department;
    @NotBlank(message = "Chức vụ không được để trống")
    private String position;

    private boolean block;

    private String b64;

    private String fileType;
    private String roleId;
}