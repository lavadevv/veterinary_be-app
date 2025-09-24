package ext.vnua.veterinary_beapp.modules.production.dto.request.productionBatchRecord;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProductionBatchRecordRequest {

    @NotNull(message = "ID không được null")
    private Long id;

    private LocalDate recordDate;
    private String stepName;
    private String result;

    private String notes;
    private String attachments;

    private Double temperature;
    private Double humidity;
    private Double pressure;

    private Integer durationMinutes;
    private Integer sequenceNumber;

    private String status; // PENDING / APPROVED / REJECTED / IN_REVIEW
}
