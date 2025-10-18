package ext.vnua.veterinary_beapp.modules.material.dto.request.material;

import ext.vnua.veterinary_beapp.modules.material.enums.MaterialForm;
import ext.vnua.veterinary_beapp.modules.material.enums.MaterialType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateMaterialRequest {

    // Giữ id để tương thích với service.updateMaterial(request)
    @NotNull(message = "ID không được để trống")
    private Long id;

    @NotBlank(message = "Mã vật liệu không được để trống")
    @Size(max = 50, message = "Mã vật liệu không được vượt quá 50 ký tự")
    private String materialCode;

    @NotBlank(message = "Tên vật liệu không được để trống")
    @Size(max = 255, message = "Tên vật liệu không được vượt quá 255 ký tự")
    private String materialName;

    @Size(max = 100, message = "Tên viết tắt không được vượt quá 100 ký tự")
    private String shortName;

    @NotNull(message = "Loại vật liệu không được để trống")
    private MaterialType materialType;

    private MaterialForm materialForm;

    @Size(max = 1000, message = "Thành phần hoạt chất không được vượt quá 1000 ký tự")
    private String activeIngredient;

    @DecimalMin(value = "0.0", message = "Độ tinh khiết không được âm")
    @DecimalMax(value = "100.0", message = "Độ tinh khiết không được vượt quá 100%")
    private BigDecimal purityPercentage;

    @DecimalMin(value = "0.0", message = "IU/gram không được âm")
    private BigDecimal iuPerGram;

    @Size(max = 50, message = "Màu sắc không được vượt quá 50 ký tự")
    private String color;

    @Size(max = 100, message = "Mùi không được vượt quá 100 ký tự")
    private String odor;

    @DecimalMin(value = "0.0", message = "Độ ẩm không được âm")
    @DecimalMax(value = "100.0", message = "Độ ẩm không được vượt quá 100%")
    private BigDecimal moistureContent;

    @DecimalMin(value = "0.0", message = "Độ nhớt không được âm")
    private BigDecimal viscosity;

    @NotBlank(message = "Đơn vị đo không được để trống")
    @Size(max = 20, message = "Đơn vị đo không được vượt quá 20 ký tự")
    private String unitOfMeasure;

    @Size(max = 500, message = "Tiêu chuẩn áp dụng không được vượt quá 500 ký tự")
    private String standardApplied;

    @Min(value = 1, message = "ID nhà cung cấp phải lớn hơn 0")
    private Long supplierId;

    @DecimalMin(value = "0.0", message = "Mức tồn kho tối thiểu không được âm")
    private BigDecimal minimumStockLevel;

    // currentStock là số dẫn xuất từ MaterialBatch → KHÔNG cho sửa qua DTO

    @DecimalMin(value = "0.0", message = "Giá cố định không được âm")
    private BigDecimal fixedPrice;

    // Cho phép null; service/mapper sẽ giữ nguyên nếu không gửi
    private Boolean requiresColdStorage;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    private Boolean isActive;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;
}
