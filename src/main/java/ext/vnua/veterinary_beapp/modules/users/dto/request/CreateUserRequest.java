package ext.vnua.veterinary_beapp.modules.users.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {
    @Email( regexp = ".+[@].+[\\.].+",message = "Email không hợp lệ")
    private String email;
    @Size(min = 6,max = 64, message = "Mật khẩu từ 6-64 kí tự")
    private String password;
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    @NotNull(message = "Phòng ban không được để trống")
    private Long departmentId;
    @NotNull(message = "Chức vụ không được để trống")
    private Long positionId;

    private String roleId;
}
