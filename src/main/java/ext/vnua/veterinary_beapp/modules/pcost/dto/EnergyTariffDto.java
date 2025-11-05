package ext.vnua.veterinary_beapp.modules.pcost.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EnergyTariffDto {
    private Long id;
    private String code;
    private String name;
    
    // Unit of Measure info
    private Long unitOfMeasureId;
    private String unitOfMeasureName;
    
    private BigDecimal pricePerUnit;
    private LocalDate effectiveDate;
    private Boolean isActive;
    private String notes;
}
