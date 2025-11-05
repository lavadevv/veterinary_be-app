package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MaterialBatchItemActiveIngredientDto {
    private Long id;
    private Long batchItemId;
    private Long activeIngredientId;
    
    // Computed fields for display
    private String activeIngredientName;
    private String activeIngredientCode;
    
    // Master data from MaterialActiveIngredient (for COA default value)
    private BigDecimal materialContentValue;  // Hàm lượng từ Material master data
    private String materialContentUnit;       // Đơn vị từ Material master data
    
    // COA (Certificate of Analysis) - Hàm lượng theo chứng nhận
    private BigDecimal coaContentValue;
    private String coaContentUnit;
    private BigDecimal coaMinValue;
    private BigDecimal coaMaxValue;
    private String coaNotes;
    
    // KQPT (Kết quả phân tích thực tế)
    private BigDecimal testContentValue;
    private String testContentUnit;
    private LocalDate testDate;
    private String testMethod;
    private String testNotes;
    
    // Computed fields
    private Boolean isQualified;  // Đạt chuẩn: 90-110%
    private BigDecimal ratioPercentage;  // Tỷ lệ KQPT/COA * 100%
    
    @Deprecated // No longer used, kept for backward compatibility
    private BigDecimal deviationPercentage;
}
