package ext.vnua.veterinary_beapp.modules.audits.dto.request;

import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import lombok.Data;

@Data
public class AuditLogCreateRequest {
    private String userId;
    private String username;
    private AuditAction action;
    private String entityName;
    private String entityId;
    private String oldValues;
    private String newValues;
    private String description;
}
