package ext.vnua.veterinary_beapp.modules.audits.dto;

import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private String userId;
    private String username;
    private AuditAction action;
    private String entityName;
    private String entityId;
    private String oldValues;
    private String newValues;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private LocalDateTime timestamp;
    private String description;
}
