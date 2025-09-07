//package ext.vnua.veterinary_beapp.modules.audits.service;
//
//import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
//import ext.vnua.veterinary_beapp.modules.audits.model.AuditLog;
//import ext.vnua.veterinary_beapp.modules.audits.repository.AuditLogRepository;
//import ext.vnua.veterinary_beapp.modules.audits.repository.CustomAuditLogQuery;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import java.time.LocalDateTime;
//
//@Service
//public class AuditService {
//
//    @Autowired
//    private AuditLogRepository auditLogRepository;
//
//    public void logAction(AuditAction action, String entityName, String entityId,
//                          String oldValues, String newValues, String description) {
//        try {
//            AuditLog auditLog = new AuditLog();
//
//            // Basic audit info
//            auditLog.setAction(action);
//            auditLog.setEntityName(entityName);
//            auditLog.setEntityId(entityId);
//            auditLog.setOldValues(oldValues);
//            auditLog.setNewValues(newValues);
//            auditLog.setDescription(description);
//            auditLog.setTimestamp(LocalDateTime.now());
//
//            // User info
//            String currentUser = getCurrentUser();
//            auditLog.setUserId(currentUser);
//            auditLog.setUsername(currentUser);
//
//            // Request info
//            HttpServletRequest request = getCurrentRequest();
//            if (request != null) {
//                auditLog.setIpAddress(getClientIpAddress(request));
//                auditLog.setUserAgent(request.getHeader("User-Agent"));
//                auditLog.setSessionId(request.getSession().getId());
//            }
//
//            auditLogRepository.save(auditLog);
//
//        } catch (Exception e) {
//            // Log error but don't throw to avoid breaking business logic
//            System.err.println("Failed to save audit log: " + e.getMessage());
//        }
//    }
//
//    private String getCurrentUser() {
//        try {
//            return SecurityContextHolder.getContext().getAuthentication().getName();
//        } catch (Exception e) {
//            return "system";
//        }
//    }
//
//    private HttpServletRequest getCurrentRequest() {
//        try {
//            ServletRequestAttributes attributes =
//                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//            return attributes != null ? attributes.getRequest() : null;
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    private String getClientIpAddress(HttpServletRequest request) {
//        String xForwardedFor = request.getHeader("X-Forwarded-For");
//        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
//            return xForwardedFor.split(",")[0].trim();
//        }
//
//        String xRealIp = request.getHeader("X-Real-IP");
//        if (xRealIp != null && !xRealIp.isEmpty()) {
//            return xRealIp;
//        }
//
//        return request.getRemoteAddr();
//    }
//
//    // Lấy audit logs theo user ID
//    public Page<AuditLog> getAllAuditLogs(CustomAuditLogQuery.AuditLogFilterParam param, PageRequest pageRequest) {
//        Specification<AuditLog> specification = CustomAuditLogQuery.getFilterAuditLog(param);
//        return auditLogRepository.findAll(specification, pageRequest);
//    }
//}
package ext.vnua.veterinary_beapp.modules.audits.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ext.vnua.veterinary_beapp.modules.audits.common.AuditMapper;
import ext.vnua.veterinary_beapp.modules.audits.dto.request.AuditLogSearchRequest;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.audits.model.AuditLog;
import ext.vnua.veterinary_beapp.modules.audits.repository.AuditLogRepository;
import ext.vnua.veterinary_beapp.modules.audits.repository.CustomAuditLogQuery;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Service
@Transactional
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public void logAction(AuditAction action, String entityName, String entityId,
                          Object oldValues, Object newValues, String description) {
        try {
            String userId = getCurrentUserId();
            String username = getCurrentUsername();

            AuditLog auditLog = new AuditLog(userId, username, action, entityName, entityId);

            if (oldValues != null) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldValues));
            }

            if (newValues != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newValues));
            }

            auditLog.setDescription(description);

            // Lấy thông tin request
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setSessionId(request.getSession().getId());
            }

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            // Log error nhưng không throw exception để không ảnh hưởng business logic
            System.err.println("Error saving audit log: " + e.getMessage());
        }
    }

    public void logCreate(String entityName, String entityId, Object newValues) {
        logAction(AuditAction.CREATE, entityName, entityId, null, newValues,
                "Created new " + entityName);
    }

    public void logUpdate(String entityName, String entityId, Object oldValues, Object newValues) {
        logAction(AuditAction.UPDATE, entityName, entityId, oldValues, newValues,
                "Updated " + entityName);
    }

    public void logDelete(String entityName, String entityId, Object oldValues) {
        logAction(AuditAction.DELETE, entityName, entityId, oldValues, null,
                "Deleted " + entityName);
    }

    public void logView(String entityName, String entityId) {
        logAction(AuditAction.VIEW, entityName, entityId, null, null,
                "Viewed " + entityName);
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    // Lấy audit logs theo user ID
    public Page<AuditLog> getAllAuditLogs(CustomAuditLogQuery.AuditLogFilterParam param, PageRequest pageRequest) {
        Specification<AuditLog> specification = CustomAuditLogQuery.getFilterAuditLog(param);
        return auditLogRepository.findAll(specification, pageRequest);
    }
}
