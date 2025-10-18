package ext.vnua.veterinary_beapp.modules.pricing.dto;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PricingLinesSaveRequest {
    @NotNull private Long formulaId;
    @NotNull @Positive private BigDecimal batchSizeKg;

    @NotNull @Valid
    private List<Line> lines;

    /** true = ghi đè toàn bộ (mặc định) */
    private Boolean replace = true;

    @Data
    public static class Line {
        @NotNull private Integer stt;
        private String manualCode;
        @NotNull private String sheetCode;
        @NotNull @Positive private BigDecimal packSize;
        @NotNull private String specName;
        @NotNull private BigDecimal profitPercent;
        private Boolean isActive = true;
    }
}
