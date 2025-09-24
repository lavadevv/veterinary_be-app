package ext.vnua.veterinary_beapp.modules.audits.common;

import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.audits.service.AuditService;
import jakarta.persistence.Id;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result = null;
        Object oldValue = null;

        String entityName = resolveEntityName(auditable, joinPoint);
        String entityId = extractIdFromArgs(joinPoint.getArgs()); // Ưu tiên lấy từ tham số (trước khi chạy)

        // Nếu là UPDATE/DELETE và bạn cần oldValue: có thể bổ sung lấy từ repository tại đây nếu cần
        if (auditable.action() == AuditAction.UPDATE || auditable.action() == AuditAction.DELETE) {
            // TODO: load oldValue nếu cần (tùy business), hiện để null cho an toàn
        }

        try {
            result = joinPoint.proceed();

            // Nếu chưa có entityId, thử lấy từ result (trả về entity/DTO có id)
            if (entityId == null) {
                entityId = extractIdFromObject(result);
            }

            String description = resolveDescription(auditable, entityName);
            auditService.logAction(auditable.action(), entityName, entityId, oldValue, result, description);

            return result;
        } catch (Exception e) {
            auditService.logAction(auditable.action(), entityName, entityId, null, null, "Failed: " + e.getMessage());
            throw e;
        }
    }

    private String resolveEntityName(Auditable auditable, ProceedingJoinPoint joinPoint) {
        if (!auditable.entityName().isEmpty()) return auditable.entityName();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        return className.substring(className.lastIndexOf('.') + 1);
    }

    private String resolveDescription(Auditable auditable, String entityName) {
        if (!auditable.description().isEmpty()) return auditable.description();
        return auditable.action().getValue() + " " + entityName;
    }

    private String extractIdFromArgs(Object[] args) {
        if (args == null) return null;
        for (Object a : args) {
            String id = extractIdFromObject(a);
            if (id != null) return id;
        }
        return null;
    }

    /** Cố gắng lấy ID theo thứ tự: primitive/String/Number → field có @Id → field "id" → method getId() */
    private String extractIdFromObject(Object obj) {
        if (obj == null) return null;

        // primitive wrapper hoặc String
        if (obj instanceof Number || obj instanceof CharSequence) {
            return obj.toString();
        }

        // Tìm field có @Id
        try {
            for (Field f : obj.getClass().getDeclaredFields()) {
                if (f.isAnnotationPresent(Id.class)) {
                    f.setAccessible(true);
                    Object v = f.get(obj);
                    return v != null ? v.toString() : null;
                }
            }
        } catch (Exception ignored) {}

        // Field "id"
        try {
            Field idField = obj.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object v = idField.get(obj);
            if (v != null) return v.toString();
        } catch (NoSuchFieldException ignored) {
        } catch (Exception ignored) {}

        // Method getId()
        try {
            Method getter = obj.getClass().getMethod("getId");
            Object v = getter.invoke(obj);
            if (v != null) return v.toString();
        } catch (Exception ignored) {}

        return null;
    }
}
