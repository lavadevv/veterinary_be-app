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
}
