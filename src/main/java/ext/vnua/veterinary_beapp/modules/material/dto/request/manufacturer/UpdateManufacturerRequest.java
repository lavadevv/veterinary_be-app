package ext.vnua.veterinary_beapp.modules.material.dto.request.manufacturer;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateManufacturerRequest {

    @NotNull(message = "ID không được để trống")
    @Min(value = 1, message = "ID phải lớn hơn 0")
    private Long id;

    @NotBlank(message = "Mã NSX không được để trống")
    @Size(max = 50, message = "Mã NSX tối đa 50 ký tự")
    private String manufacturerCode;

    @NotBlank(message = "Tên NSX không được để trống")
    @Size(max = 255, message = "Tên NSX tối đa 255 ký tự")
    private String manufacturerName;

    @Size(max = 100, message = "Quốc gia tối đa 100 ký tự")
    private String countryOfOrigin;

    @Size(max = 255, message = "Nhà phân phối chính thức tối đa 255 ký tự")
    private String officialDistributorName;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Số điện thoại nhà phân phối không hợp lệ")
    @Size(max = 30, message = "Số điện thoại tối đa 30 ký tự")
    private String officialDistributorPhone;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    private Boolean isActive;

    private String notes;
}
