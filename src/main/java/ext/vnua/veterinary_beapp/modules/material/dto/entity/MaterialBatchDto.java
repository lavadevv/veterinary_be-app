package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import ext.vnua.veterinary_beapp.modules.material.enums.BatchStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.TestStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class MaterialBatchDto {
    private Long id;
    
    // IDs thay vì full DTOs
    private Long materialId;
    private Long locationId;
    private Long supplierId;
    private Long manufacturerId;
    
    private String batchNumber;
    private String internalBatchCode;
    private String manufacturerBatchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private LocalDate receivedDate;
    private BigDecimal receivedQuantity;
    private BigDecimal currentQuantity;
    private BigDecimal unitPrice;
    private BigDecimal taxPercent;
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private TestStatus testStatus;
    private UsageStatus usageStatus;
    private String countryOfOrigin;
    private String invoiceNumber;
    private String coaNumber;
    private String testReportNumber;
    private String testResults;
    private String quarantineReason;
    private String coaFilePath;
    private String msdsFilePath;
    private String testCertificatePath;
    private String notes;
    private BigDecimal reservedQuantity;
    private BigDecimal availableQuantity;
    private String shelfLocation;
    private String imagePath;
    
    // Container status
    private BatchStatus batchStatus;
    
    // Items in this batch
    private List<MaterialBatchItemDto> batchItems; // Full item details for display
    private List<Long> batchItemIds; // Just IDs (lightweight alternative)
    private Integer totalItemsCount; // Số lượng items trong lô
    
    // ===== COMPUTED SUMMARY FIELDS (từ items) =====
    private BigDecimal totalCurrentQuantity; // Tổng số lượng hiện tại từ tất cả items
    private Integer readyItemsCount; // Số items sẵn sàng (testStatus=DAT && usageStatus=SAN_SANG)
    private Integer quarantinedItemsCount; // Số items cách ly (testStatus=KHONG_DAT || usageStatus=CACH_LY)
    private Integer nearExpiryItemsCount; // Số items sắp hết hạn (trong 30 ngày)
    private Integer expiredItemsCount; // Số items đã hết hạn
    // totalAmount: Tổng tiền của batch (đã có sẵn, được tính từ items)
    
    // NOTE: Active Ingredients moved to MaterialBatchItemActiveIngredient
    // Use MaterialBatchItem.batchItemActiveIngredients instead
    
    // Computed fields
    private String materialName;
    private String materialCode;
    private String internationalName;
    private String unitName;
    private String supplierName;
    private String manufacturerName;
    private String locationCode;
    private String formTypeName;

    @Override
    public String toString() {
        return String.format(
                "Lô nguyên liệu:\n" +
                        "   - ID: %d\n" +
                        "   - Số lô: %s\n" +
                        "   - Mã nội bộ: %s\n" +
                        "   - Số lô NSX: %s\n" +
                        "   - Ngày sản xuất: %s\n" +
                        "   - Hạn sử dụng: %s\n" +
                        "   - Ngày nhập: %s\n" +
                        "   - Số lượng nhập: %s\n" +
                        "   - Số lượng còn: %s\n" +
                        "   - Đơn giá: %s\n" +
                        "   - Trạng thái kiểm nghiệm: %s\n" +
                        "   - Trạng thái sử dụng: %s\n" +
                        "   - Ghi chú: %s\n",
                id,
                batchNumber,
                internalBatchCode,
                manufacturerBatchNumber,
                manufacturingDate,
                expiryDate,
                receivedDate,
                receivedQuantity,
                currentQuantity,
                unitPrice,
                testStatus != null ? testStatus.name() : "Chưa kiểm nghiệm",
                usageStatus != null ? usageStatus.name() : "Chưa xác định",
                notes
        );
    }

}
