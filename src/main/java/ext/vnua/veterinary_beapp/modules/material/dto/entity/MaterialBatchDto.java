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
