package ext.vnua.veterinary_beapp.modules.audits.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ext.vnua.veterinary_beapp.config.JwtConfig;
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
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Transactional
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final JwtConfig jwtConfig; // <-- Lấy userId/email từ JWT

    @Autowired
    private ObjectMapper objectMapper;

    public void logAction(AuditAction action, String entityName, String entityId,
                          Object oldValues, Object newValues, String description) {
        try {
            String[] principal = getPrincipalFromJwt();
            String userId = principal[0];
            String username = principal[1];

            AuditLog auditLog = new AuditLog(userId, username, action, entityName, entityId);

            if (oldValues != null) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldValues));
            }
            if (newValues != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newValues));
            }
            auditLog.setDescription(description);

            // Thông tin request (IP/User-Agent/Session)
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                if (request.getSession(false) != null) {
                    auditLog.setSessionId(request.getSession().getId());
                }
            }

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            // Không làm hỏng luồng nghiệp vụ
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

    // --- Helpers ---

    /** Lấy userId/email từ JWT trong header Authorization; fallback "system" */
    private String[] getPrincipalFromJwt() {
        try {
            HttpServletRequest req = getCurrentRequest();
            if (req == null) return new String[]{"system", "system"};

            String authz = req.getHeader("Authorization");
            if (authz == null || !authz.startsWith("Bearer ")) {
                return new String[]{"system", "system"};
            }
            String token = authz.substring(7);

            Long id = jwtConfig.getUserIdFromJWTClaims(token);
            String email = jwtConfig.getUserEmailFromJWT(token);

            String userId = (id != null ? String.valueOf(id) : "system");
            String username = (email != null ? email : "system");
            return new String[]{userId, username};
        } catch (Exception e) {
            return new String[]{"system", "system"};
        }
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

    // Lấy audit logs theo điều kiện
    public Page<AuditLog> getAllAuditLogs(CustomAuditLogQuery.AuditLogFilterParam param, PageRequest pageRequest) {
        Specification<AuditLog> specification = CustomAuditLogQuery.getFilterAuditLog(param);
        return auditLogRepository.findAll(specification, pageRequest);
    }
}
