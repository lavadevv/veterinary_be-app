package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import ext.vnua.veterinary_beapp.modules.material.enums.MaterialForm;
import ext.vnua.veterinary_beapp.modules.material.enums.MaterialType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MaterialDto {
    private Long id;
    private String materialCode;
    private String materialName;
    private String shortName;
    private MaterialType materialType;
    private MaterialForm materialForm;
    private String activeIngredient;
    private Double purityPercentage;
    private Double iuPerGram;
    private String color;
    private String odor;
    private Double moistureContent;
    private Double viscosity;
    private String unitOfMeasure;
    private String standardApplied;
    private SupplierDto supplierDto;
    private Double minimumStockLevel;
    private Double currentStock;
    private Double fixedPrice;
    private Boolean requiresColdStorage;
    private String specialHandling;
    private Boolean isActive;
    private String notes;
}
