package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import ext.vnua.veterinary_beapp.modules.material.model.MaterialTransaction.TransactionType;
import ext.vnua.veterinary_beapp.modules.users.dto.entity.UserDto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MaterialTransactionDto {
    private Long id;
    private MaterialBatchDto materialBatchDto;
    private TransactionType transactionType;
    private LocalDateTime transactionDate;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalValue;
    private String referenceDocument;
    private String productionOrderId;
    private LocationDto fromLocationDto;
    private LocationDto toLocationDto;
    private String reason;
    private String notes;
    private UserDto createdByDto;
    private UserDto approvedByDto;

    @Override
    public String toString() {
        return String.format(
                "Giao dịch nguyên liệu:\n" +
                        "   - ID: %d\n" +
                        "   - Loại giao dịch: %s\n" +
                        "   - Ngày giao dịch: %s\n" +
                        "   - Số lượng: %s\n" +
                        "   - Đơn giá: %s\n" +
                        "   - Tổng giá trị: %s\n" +
                        "   - Tài liệu tham chiếu: %s\n" +
                        "   - Lệnh sản xuất: %s\n" +
                        "   - Vị trí từ: %s\n" +
                        "   - Vị trí đến: %s\n" +
                        "   - Lý do: %s\n" +
                        "   - Ghi chú: %s\n" +
                        "   - Người tạo: %s\n" +
                        "   - Người duyệt: %s\n",
                id,
                transactionType != null ? transactionType.name() : "Không rõ",
                transactionDate != null ? transactionDate.toString() : "Không rõ",
                quantity,
                unitPrice,
                totalValue,
                referenceDocument,
                productionOrderId,
                fromLocationDto != null ? fromLocationDto.getLocationCode() : "Không rõ",
                toLocationDto != null ? toLocationDto.getLocationCode() : "Không rõ",
                reason,
                notes,
                createdByDto != null ? createdByDto.getEmail() : "Không rõ",
                approvedByDto != null ? approvedByDto.getEmail() : "Chưa duyệt"
        );
    }

}