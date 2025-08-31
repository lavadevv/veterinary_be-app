package ext.vnua.veterinary_beapp.modules.users.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {
    @Email( regexp = ".+[@].+[\\.].+",message = "Email không hợp lệ")
    private String email;
    @Size(min = 6,max = 15, message = "Mật khẩu từ 6-15 kí tự")
    private String password;
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

    private String roleId;
}
