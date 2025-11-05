package ext.vnua.veterinary_beapp.modules.material.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for creating a new MaterialBatch with multiple items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaterialBatchRequest {

    private String batchNumber;
    
    private String internalBatchCode;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedDate;
    
    private Long supplierId;
    
    private Long manufacturerId;
    
    private String countryOfOrigin;
    
    private String invoiceNumber;
    
    private Long locationId; // Default location for all items (can be overridden per item)
    
    private String notes;
    
    @Builder.Default
    private List<CreateMaterialBatchItemRequest> items = new ArrayList<>();
}

/**
 * DTO for creating a MaterialBatchItem
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CreateMaterialBatchItemRequest {
    
    private Long materialId;
    
    private String internalItemCode;
    
    private String manufacturerBatchNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate manufacturingDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    private BigDecimal receivedQuantity;
    
    private BigDecimal unitPrice;
    
    @Builder.Default
    private BigDecimal taxPercent = BigDecimal.ZERO;
    
    private Long locationId; // Override batch location if needed
    
    private String shelfLocation;
    
    private String imagePath;
    
    private String coaNumber;
    
    private String testReportNumber;
    
    private String notes;
    
    @Builder.Default
    private List<CreateMaterialBatchItemActiveIngredientRequest> activeIngredients = new ArrayList<>();
}

/**
 * DTO for creating MaterialBatchItemActiveIngredient
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CreateMaterialBatchItemActiveIngredientRequest {
    
    private Long activeIngredientId;
    
    // COA values
    private BigDecimal coaContentValue;
    private String coaContentUnit;
    private BigDecimal coaMinValue;
    private BigDecimal coaMaxValue;
    private String coaNotes;
    
    // Test values (optional at creation, can be added later)
    private BigDecimal testContentValue;
    private String testContentUnit;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate testDate;
    
    private String testMethod;
    private String testNotes;
}
