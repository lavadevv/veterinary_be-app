package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import ext.vnua.veterinary_beapp.modules.material.enums.TestStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class MaterialBatchItemDto {
    private Long id;
    private Long batchId;
    private Long materialId;
    private Long locationId;
    private Long supplierId;
    private Long manufacturerId;
    
    private String internalItemCode;
    private String manufacturerBatchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    
    private BigDecimal receivedQuantity;
    private BigDecimal currentQuantity;
    private BigDecimal unitPrice;
    private BigDecimal taxPercent;
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    
    private TestStatus testStatus;
    private UsageStatus usageStatus;
    
    private String notes;
    
    // Computed fields (optional - để hiển thị, không cần fetch nested objects)
    private String materialName;
    private String materialCode;
    private String internationalName; // International name from material
    private String unitOfMeasure; // Unit from material
    private String locationCode;
    private String locationShelf; // Shelf position from location
    private String supplierName;
    private String manufacturerName;
    
    // Active ingredient IDs for this item (để nhẹ, fetch details nếu cần)
    private List<Long> activeIngredientIds;
    private Integer totalActiveIngredientsCount;
}
