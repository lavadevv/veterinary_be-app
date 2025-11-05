package ext.vnua.veterinary_beapp.modules.productionplan.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request để tính toán nhu cầu nguyên vật liệu cho lệnh sản xuất
 */
@Data
public class CalculateMaterialRequirementRequest {
    
    @NotNull(message = "Formula ID is required")
    private Long formulaId;
    
    @NotNull(message = "Batch size is required")
    @DecimalMin(value = "0.001", message = "Batch size must be greater than 0")
    private BigDecimal batchSize;
    
    /**
     * Unit của batch size (g, kg, l)
     * Default: sẽ lấy từ formula.basisUnit
     */
    private String batchUnit;
}
