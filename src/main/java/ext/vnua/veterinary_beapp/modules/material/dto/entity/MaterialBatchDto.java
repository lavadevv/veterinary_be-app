package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import ext.vnua.veterinary_beapp.modules.material.enums.TestStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MaterialBatchDto {
    private Long id;
    private MaterialDto materialDto;
    private String batchNumber;
    private String internalBatchCode;
    private String manufacturerBatchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private LocalDate receivedDate;
    private BigDecimal receivedQuantity;
    private BigDecimal currentQuantity;
    private BigDecimal unitPrice;
    private TestStatus testStatus;
    private UsageStatus usageStatus;
    private LocationDto locationDto;
    private String coaNumber;
    private String testReportNumber;
    private String testResults;
    private String quarantineReason;
    private String coaFilePath;
    private String msdsFilePath;
    private String testCertificatePath;
    private String notes;

}
