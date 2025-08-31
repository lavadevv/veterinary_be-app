package ext.vnua.veterinary_beapp.modules.audits.common;

import ext.vnua.veterinary_beapp.modules.audits.dto.AuditLogDTO;
import ext.vnua.veterinary_beapp.modules.audits.model.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditMapper {
    AuditLog toAuditLog(AuditLogDTO auditLogDTO);
    AuditLogDTO toAuditLogDTO(AuditLog auditLog);
}
