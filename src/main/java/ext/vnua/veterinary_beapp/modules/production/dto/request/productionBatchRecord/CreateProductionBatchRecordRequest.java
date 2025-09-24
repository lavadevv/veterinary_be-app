package ext.vnua.veterinary_beapp.modules.production.dto.request.productionBatchRecord;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateProductionBatchRecordRequest {

    @NotNull(message = "Production Order ID không được null")
    private Long productionOrderId;

    @NotNull(message = "Ngày ghi nhận không được null")
    private LocalDate recordDate;

    @NotNull(message = "Tên bước không được để trống")
    @Size(max = 100, message = "Tên bước không được vượt quá 100 ký tự")
    private String stepName;

    private String result;

    private String notes;
    private String attachments;

    private Double temperature;
    private Double humidity;
    private Double pressure;

    private Integer durationMinutes;

    private Integer sequenceNumber; // Nếu null → service sẽ tự sinh
}
