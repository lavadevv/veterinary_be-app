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
