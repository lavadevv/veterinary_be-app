package ext.vnua.veterinary_beapp.modules.product.dto;

import ext.vnua.veterinary_beapp.modules.product.enums.ProductBatchStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProductBatchDto {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private Long formulaId;
    private String formulaVersion;
    private String batchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private BigDecimal plannedQuantity;
    private BigDecimal actualQuantity;
    private BigDecimal yieldPercentage;
    private BigDecimal rejectedQuantity;
    private BigDecimal currentStock;
    private ProductBatchStatus status;
    private Long locationId;
    private String locationCode;
    private Long qcApprovedById;
    private LocalDateTime qcApprovedAt;
    private Long productionOrderId;
    private String qcCertificatePath;
    private String notes;
}
