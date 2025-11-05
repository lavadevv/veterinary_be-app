package ext.vnua.veterinary_beapp.modules.material.dto.request.supplier;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateSupplierRequest {

    @NotNull(message = "ID nhà cung cấp không được để trống")
    @Min(value = 1, message = "ID nhà cung cấp phải lớn hơn 0")
    private Long id;

    @NotBlank(message = "Mã nhà cung cấp không được để trống")
    @Size(max = 50, message = "Mã nhà cung cấp không được vượt quá 50 ký tự")
    private String supplierCode;

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    @Size(max = 255, message = "Tên nhà cung cấp không được vượt quá 255 ký tự")
    private String supplierName;

    private Long manufacturerId;

    private String address;

    @Size(max = 100, message = "Số đăng ký kinh doanh không được vượt quá 100 ký tự")
    private String registrationNumber;

    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Số điện thoại không hợp lệ")
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @Email(message = "Định dạng email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Size(max = 255, message = "Tên người liên hệ không được vượt quá 255 ký tự")
    private String contactPerson;

    @Size(max = 100, message = "Chứng chỉ GMP không được vượt quá 100 ký tự")
    private String gmpCertificate;

    private LocalDate gmpExpiryDate;

    @Size(max = 100, message = "Quốc gia xuất xứ không được vượt quá 100 ký tự")
    private String countryOfOrigin;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    private Boolean isActive;

    private String notes;
}