package ext.vnua.veterinary_beapp.modules.production.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProductionBatchRecordDto {
    private Long id;
    private Long productionOrderId;
    private String orderCode;

    private LocalDate recordDate;
    private String stepName;
    private String result;

    private Long approvedById;
    private String approvedByName;
    private LocalDateTime approvedDate;

    private String status;
    private String notes;
    private String attachments;

    private Double temperature;
    private Double humidity;
    private Double pressure;

    private Integer durationMinutes;
    private Integer sequenceNumber;
}
