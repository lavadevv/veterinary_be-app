//package ext.vnua.veterinary_beapp.modules.audits.common;
//
//import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
//import ext.vnua.veterinary_beapp.modules.audits.service.AuditService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.persistence.Id;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//
//@Aspect
//@Component
//public class AuditAspect {
//
//    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);
//
//    @Autowired
//    private AuditService auditService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Around("@annotation(auditable)")
//    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
//        Object result = null;
//        Object oldValue = null;
//        String entityId = null;
//        String entityName = getEntityName(auditable, joinPoint);
//
//        try {
//            // Extract entity ID từ arguments
//            entityId = extractEntityIdFromArgs(joinPoint.getArgs());
//
//            // Lấy old value cho UPDATE và DELETE
//            if (auditable.action() == AuditAction.UPDATE || auditable.action() == AuditAction.DELETE) {
//                oldValue = getOldValue(entityId, joinPoint);
//            }
//
//            // Thực hiện method
//            result = joinPoint.proceed();
//
//            // Lấy entity ID từ result nếu là CREATE
//            if (auditable.action() == AuditAction.CREATE && entityId == null) {
//                entityId = extractEntityId(result);
//            }
//
//            // Log audit success
//            logAuditAction(auditable, entityName, entityId, oldValue, result, null);
//
//        } catch (Exception e) {
//            logger.error("Error in audited method: {}", e.getMessage(), e);
//            logAuditAction(auditable, entityName, entityId, oldValue, null, e.getMessage());
//            throw e;
//        }
//
//        return result;
//    }
//
//    private String getEntityName(Auditable auditable, ProceedingJoinPoint joinPoint) {
//        if (!auditable.entityName().isEmpty()) {
//            return auditable.entityName();
//        }
//
//        String className = joinPoint.getSignature().getDeclaringTypeName();
//        return className.substring(className.lastIndexOf('.') + 1)
//                .replace("Service", "")
//                .replace("Controller", "");
//    }
//
//    private String extractEntityIdFromArgs(Object[] args) {
//        if (args == null || args.length == 0) {
//            return null;
//        }
//
//        // Kiểm tra argument đầu tiên
//        Object firstArg = args[0];
//
//        // Nếu là primitive type (String, Long, Integer)
//        if (firstArg instanceof String || firstArg instanceof Number) {
//            return firstArg.toString();
//        }
//
//        // Nếu là entity object, extract ID
//        return extractEntityId(firstArg);
//    }
//
//    private String extractEntityId(Object entity) {
//        if (entity == null) {
//            return null;
//        }
//
//        try {
//            // Tìm field có @Id annotation
//            Field[] fields = entity.getClass().getDeclaredFields();
//            for (Field field : fields) {
//                if (field.isAnnotationPresent(Id.class)) {
//                    field.setAccessible(true);
//                    Object idValue = field.get(entity);
//                    return idValue != null ? idValue.toString() : null;
//                }
//            }
//
//            // Fallback: tìm field "id"
//            try {
//                Field idField = entity.getClass().getDeclaredField("id");
//                idField.setAccessible(true);
//                Object idValue = idField.get(entity);
//                return idValue != null ? idValue.toString() : null;
//            } catch (NoSuchFieldException ignored) {}
//
//            // Fallback: tìm method getId()
//            try {
//                Method getIdMethod = entity.getClass().getMethod("getId");
//                Object idValue = getIdMethod.invoke(entity);
//                return idValue != null ? idValue.toString() : null;
//            } catch (Exception ignored) {}
//
//        } catch (Exception e) {
//            logger.warn("Failed to extract entity ID: {}", e.getMessage());
//        }
//
//        return null;
//    }
//
//    private Object getOldValue(String entityId, ProceedingJoinPoint joinPoint) {
//        if (entityId == null) {
//            return null;
//        }
//
//        try {
//            Object target = joinPoint.getTarget();
//            String methodName = joinPoint.getSignature().getName();
//
//            // Thử tìm method findById
//            Method findByIdMethod = null;
//            try {
//                findByIdMethod = target.getClass().getMethod("findById", String.class);
//            } catch (NoSuchMethodException e) {
//                try {
//                    findByIdMethod = target.getClass().getMethod("findById", Long.class);
//                    entityId = entityId; // keep as string, will be converted
//                } catch (NoSuchMethodException e2) {
//                    logger.warn("No findById method found in {}", target.getClass().getSimpleName());
//                    return null;
//                }
//            }
//
//            if (findByIdMethod != null) {
//                Object oldEntity = findByIdMethod.invoke(target,
//                        findByIdMethod.getParameterTypes()[0] == Long.class ?
//                                Long.parseLong(entityId) : entityId);
//                return oldEntity;
//            }
//
//        } catch (Exception e) {
//            logger.warn("Failed to get old value for ID {}: {}", entityId, e.getMessage());
//        }
//
//        return null;
//    }
//
//    private void logAuditAction(Auditable auditable, String entityName, String entityId,
//                                Object oldValue, Object newValue, String errorMessage) {
//        try {
//            // Serialize objects to JSON
//            String oldValuesJson = oldValue != null ? objectMapper.writeValueAsString(oldValue) : null;
//            String newValuesJson = newValue != null ? objectMapper.writeValueAsString(newValue) : null;
//
//            String description = getDescription(auditable, entityName, errorMessage);
//
//            auditService.logAction(auditable.action(), entityName, entityId,
//                    oldValuesJson, newValuesJson, description);
//
//        } catch (Exception e) {
//            logger.error("Failed to log audit action: {}", e.getMessage(), e);
//        }
//    }
//
//    private String getDescription(Auditable auditable, String entityName, String errorMessage) {
//        if (!auditable.description().isEmpty()) {
//            return errorMessage != null ? auditable.description() + " - Failed: " + errorMessage
//                    : auditable.description();
//        }
//
//        String baseDescription = auditable.action().getValue() + " " + entityName;
//        return errorMessage != null ? baseDescription + " - Failed: " + errorMessage
//                : baseDescription;
//    }
//}
package ext.vnua.veterinary_beapp.modules.audits.common;

import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.audits.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {

    @Autowired
    private AuditService auditService;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result = null;
        Object oldValue = null;

        // Nếu là UPDATE, lấy giá trị cũ trước khi thực hiện
        if (auditable.action() == AuditAction.UPDATE) {
            // Logic để lấy old value (tùy thuộc vào business logic)
        }

        try {
            result = joinPoint.proceed();

            // Log sau khi thực hiện thành công
            String entityName = auditable.entityName().isEmpty() ?
                    joinPoint.getSignature().getDeclaringTypeName() : auditable.entityName();

            String description = auditable.description().isEmpty() ?
                    auditable.action().getValue() + " " + entityName : auditable.description();

            auditService.logAction(auditable.action(), entityName,
                    extractEntityId(result), oldValue, result, description);

        } catch (Exception e) {
            // Log lỗi nếu có
            auditService.logAction(auditable.action(), auditable.entityName(),
                    null, null, null, "Failed: " + e.getMessage());
            throw e;
        }

        return result;
    }

    private String extractEntityId(Object entity) {
        // Logic để extract ID từ entity
        // Có thể sử dụng reflection hoặc instanceof checks
        return entity != null ? entity.toString() : null;
    }
}
