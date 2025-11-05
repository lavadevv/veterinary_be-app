package ext.vnua.veterinary_beapp.modules.product.dto.request.productbrand;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO cho tạo/cập nhật ProductBrand
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpsertProductBrandRequest {
    
    @NotNull(message = "Product ID không được để trống")
    private Long productId;
    
    @NotNull(message = "Brand ID không được để trống")
    private Long brandId;
    
    private Long productionCostSheetId;
    
    @Size(max = 300, message = "Quy cách đóng gói không được vượt quá 300 ký tự")
    private String packagingSpecification;
    
    @Size(max = 150, message = "Số đăng ký không được vượt quá 150 ký tự")
    private String registrationNumber;
    
    @Size(max = 150, message = "Mã lưu hành không được vượt quá 150 ký tự")
    private String circulationCode;
    
    private String qualityStandard;
    
    /**
     * Chi phí nguyên liệu (có thể tính từ Formula hoặc nhập thủ công)
     */
    @DecimalMin(value = "0.0", message = "Chi phí nguyên liệu phải >= 0")
    private BigDecimal materialCost;
    
    /**
     * Chi phí sản xuất (có thể lấy từ ProductionCostSheet hoặc nhập thủ công)
     */
    @DecimalMin(value = "0.0", message = "Chi phí sản xuất phải >= 0")
    private BigDecimal productionUnitCost;
    
    /**
     * Tỷ lệ lợi nhuận (0-100)
     */
    @NotNull(message = "Tỷ lệ lợi nhuận không được để trống")
    @DecimalMin(value = "0.0", message = "Tỷ lệ lợi nhuận phải >= 0")
    @DecimalMax(value = "100.0", message = "Tỷ lệ lợi nhuận phải <= 100")
    private BigDecimal profitMarginPercentage;
    
    /**
     * Tỷ lệ VAT (0-100)
     */
    @NotNull(message = "Tỷ lệ VAT không được để trống")
    @DecimalMin(value = "0.0", message = "Tỷ lệ VAT phải >= 0")
    @DecimalMax(value = "100.0", message = "Tỷ lệ VAT phải <= 100")
    private BigDecimal vatPercentage;
    
    private Boolean isActive;
    
    private String notes;
}
