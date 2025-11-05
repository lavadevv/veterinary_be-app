package ext.vnua.veterinary_beapp.modules.material.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for MaterialBatch response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialBatchResponse {
    
    private Long id;
    private String batchNumber;
    private String internalBatchCode;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedDate;
    
    private SupplierSummary supplier;
    private ManufacturerSummary manufacturer;
    private String countryOfOrigin;
    private String invoiceNumber;
    private LocationSummary location;
    
    private BigDecimal totalAmount;
    private Integer totalItemsCount;
    private String batchStatus;
    private String notes;
    
    @Builder.Default
    private List<MaterialBatchItemResponse> items = new ArrayList<>();
    
    // Audit fields
    private String createdBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;
    
    private String lastModifiedBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModifiedDate;
}

/**
 * DTO for MaterialBatchItem in response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class MaterialBatchItemResponse {
    
    private Long id;
    private MaterialSummary material;
    private String internalItemCode;
    private String manufacturerBatchNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate manufacturingDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    private BigDecimal receivedQuantity;
    private BigDecimal currentQuantity;
    private BigDecimal reservedQuantity;
    private BigDecimal availableQuantity;
    
    private BigDecimal unitPrice;
    private BigDecimal taxPercent;
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    
    private String testStatus;
    private String testStatusDisplay;
    private String usageStatus;
    private String usageStatusDisplay;
    
    private LocationSummary location;
    private String shelfLocation;
    private String imagePath;
    
    private String coaNumber;
    private String testReportNumber;
    private String coaFilePath;
    private String msdsFilePath;
    private String testCertificatePath;
    
    private String qualificationStatus; // "Đạt", "Không đạt", "Chưa có dữ liệu"
    private Boolean isQualified;
    
    private String notes;
    
    @Builder.Default
    private List<MaterialBatchItemActiveIngredientResponse> activeIngredients = new ArrayList<>();
}

/**
 * DTO for MaterialBatchItemActiveIngredient in response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class MaterialBatchItemActiveIngredientResponse {
    
    private Long id;
    private ActiveIngredientSummary activeIngredient;
    
    // COA values
    private BigDecimal coaContentValue;
    private String coaContentUnit;
    private BigDecimal coaMinValue;
    private BigDecimal coaMaxValue;
    private String coaNotes;
    
    // Test values
    private BigDecimal testContentValue;
    private String testContentUnit;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate testDate;
    
    private String testMethod;
    private String testNotes;
    
    // Calculated fields
    private Boolean isQualified;
    private BigDecimal deviationPercentage;
    private String qualificationStatus;
}

// ============ Summary DTOs ============

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class MaterialSummary {
    private Long id;
    private String materialCode;
    private String materialName;
    private String internationalName;
    private UnitOfMeasureSummary unitOfMeasure;
    private String categoryName;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class UnitOfMeasureSummary {
    private Long id;
    private String unitCode;
    private String unitName;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class SupplierSummary {
    private Long id;
    private String supplierCode;
    private String supplierName;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ManufacturerSummary {
    private Long id;
    private String manufacturerCode;
    private String manufacturerName;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class LocationSummary {
    private Long id;
    private String locationCode;
    private String locationName;
    private WarehouseSummary warehouse;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class WarehouseSummary {
    private Long id;
    private String warehouseCode;
    private String warehouseName;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ActiveIngredientSummary {
    private Long id;
    private String ingredientCode;
    private String ingredientName;
    private String chemicalFormula;
}
