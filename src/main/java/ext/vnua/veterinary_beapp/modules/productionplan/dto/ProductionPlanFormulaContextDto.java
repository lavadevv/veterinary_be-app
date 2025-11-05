package ext.vnua.veterinary_beapp.modules.productionplan.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductionPlanFormulaContextDto {
    private Long formulaId;
    private String formulaCode;
    private String formulaName;
    private String version;
    private BigDecimal defaultBatchSize;
    private Boolean active;

    private List<ProductInfo> products = new ArrayList<>();

    @Data
    public static class ProductInfo {
        private Long productId;
        private String productCode;
        private String productName;
        private String unitOfMeasure;
        private Boolean active;
    }
}
