package ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpsertFormulaRequest {
    @NotNull private Long productId;

    @NotBlank private String version;     // v1.0, v1.1, ...
    @NotNull private BigDecimal batchSize;

    private String description;
    private String sopFilePath;
    private Boolean isActive = true;      // active formula

    @Valid
    @NotNull
    private List<FormulaItem> items;

    @Data
    public static class FormulaItem {
        @NotNull private Long materialId;
        @NotNull private BigDecimal quantity;
        @NotBlank private String unit;
        private BigDecimal percentage; // optional
        private Boolean isCritical = false;
        private String notes;
    }
}