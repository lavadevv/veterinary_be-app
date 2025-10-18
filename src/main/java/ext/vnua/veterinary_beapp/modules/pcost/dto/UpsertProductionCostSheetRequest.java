package ext.vnua.veterinary_beapp.modules.pcost.dto;

import ext.vnua.veterinary_beapp.modules.pcost.enums.CostItemType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpsertProductionCostSheetRequest {
    private Long productId;

    @NotBlank private String sheetCode;
    private String sheetName;
    private LocalDate effectiveDate;
    @NotNull @Min(1) private Integer specUnits; // ví dụ 36
    private Boolean isActive = true;
    private String notes;

    @Valid @NotNull
    private List<Item> items;

    @Data
    public static class Item {
        private Integer orderNo;

        @NotNull
        private CostItemType itemType; // MATERIAL / LABOR / ENERGY

        // chỉ 1 trong các ID dưới tương ứng với itemType
        private Long materialId;       // khi MATERIAL
        private Long laborRateId;      // khi LABOR
        private Long energyTariffId;   // khi ENERGY

        @NotNull @DecimalMin("0.000")
        private BigDecimal quantity;   // số lượng (đơn vị phụ thuộc từng loại)
    }
}
