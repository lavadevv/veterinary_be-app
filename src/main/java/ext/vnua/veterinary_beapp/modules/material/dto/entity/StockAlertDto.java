package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import ext.vnua.veterinary_beapp.modules.material.model.StockAlert.AlertType;
import ext.vnua.veterinary_beapp.modules.users.dto.entity.UserDto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockAlertDto {
    private Long id;
    private MaterialDto materialDto;
    private MaterialBatchDto materialBatchDto;
    private AlertType alertType;
    private String alertMessage;
    private LocalDateTime alertDate;
    private Boolean isResolved;
    private LocalDateTime resolvedDate;
    private UserDto resolvedByDto;
    private String resolutionNotes;

    @Override
    public String toString() {
        return String.format(
                "Cảnh báo tồn kho:\n" +
                        "   - ID: %d\n" +
                        "   - Nguyên liệu: %s\n" +
                        "   - Lô: %s\n" +
                        "   - Loại cảnh báo: %s\n" +
                        "   - Nội dung cảnh báo: %s\n" +
                        "   - Ngày cảnh báo: %s\n" +
                        "   - Đã xử lý: %s\n" +
                        "   - Ngày xử lý: %s\n" +
                        "   - Người xử lý: %s\n" +
                        "   - Ghi chú xử lý: %s\n",
                id,
                materialDto != null ? materialDto.getMaterialName() : "Không rõ",
                materialBatchDto != null ? materialBatchDto.getBatchNumber() : "Không rõ",
                alertType != null ? alertType.name() : "Không rõ",
                alertMessage,
                alertDate != null ? alertDate.toString() : "Không rõ",
                Boolean.TRUE.equals(isResolved) ? "Đã xử lý" : "Chưa xử lý",
                resolvedDate != null ? resolvedDate.toString() : "Chưa có",
                resolvedByDto != null ? resolvedByDto.getEmail() : "Chưa có",
                resolutionNotes
        );
    }

}
