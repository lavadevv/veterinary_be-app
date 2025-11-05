package ext.vnua.veterinary_beapp.modules.productionplan.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request để mô phỏng tiêu hao nguyên vật liệu theo FIFO
 * Dùng cho "Lệnh xuất nguyên liệu"
 */
@Data
public class SimulateMaterialConsumptionRequest {
    
    /**
     * Formula ID - required if formulaCode not provided
     */
    private Long formulaId;
    
    /**
     * Formula Code - alternative to formulaId
     */
    private String formulaCode;
    
    @NotNull(message = "Batch size is required")
    @DecimalMin(value = "0.001", message = "Batch size must be greater than 0")
    private BigDecimal batchSize;
    
    /**
     * Unit của batch size (g, kg, l)
     * Default: sẽ lấy từ formula.basisUnit
     */
    private String batchUnit;
    
    /**
     * Optional: Lot ID để hiển thị thông tin lot
     */
    private Long lotId;
}
