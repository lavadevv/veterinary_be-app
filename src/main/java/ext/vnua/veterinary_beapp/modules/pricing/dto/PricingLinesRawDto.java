package ext.vnua.veterinary_beapp.modules.pricing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class PricingLinesRawDto {
    private Long productId;
    private Long formulaId;
    private BigDecimal batchSizeKg;
    private LocalDateTime updatedAt;
    private List<Line> lines;

    @Data
    @Builder
    @AllArgsConstructor
    public static class Line {
        private Long id;
        private Integer stt;
        private String manualCode;
        private String sheetCode;
        private BigDecimal packSize;
        private String specName;
        private BigDecimal profitPercent;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
