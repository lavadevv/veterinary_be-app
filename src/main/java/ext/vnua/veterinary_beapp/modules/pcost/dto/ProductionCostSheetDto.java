// ext/vnua/veterinary_beapp/modules/pcost/dto/ProductionCostSheetDto.java
package ext.vnua.veterinary_beapp.modules.pcost.dto;

import ext.vnua.veterinary_beapp.modules.pcost.enums.CostItemType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProductionCostSheetDto {
    private Long id;
    private Long productId;
    private String sheetCode;
    private String sheetName;
    private LocalDate effectiveDate;
    private Integer specUnits;
    private Boolean isActive;
    private String notes;
    private BigDecimal totalAmount;
    private BigDecimal unitCost;
    private List<Item> items;

    @Data
    public static class Item {
        private Long id;
        private Integer orderNo;
        private String costCode;
        private String costName;
        private String unitOfMeasure;
        private java.math.BigDecimal quantity;
        private java.math.BigDecimal unitPrice;
        private java.math.BigDecimal amount;

        // NEW (để FE biết dòng này là loại gì & id tham chiếu)
        private CostItemType itemType;
        private Long materialId;
        private Long laborRateId;
        private Long energyTariffId;
    }
}
