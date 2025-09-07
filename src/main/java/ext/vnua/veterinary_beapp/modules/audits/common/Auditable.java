package ext.vnua.veterinary_beapp.modules.audits.common;

//import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
//
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//
//@Target(ElementType.METHOD)
//@Retention(RetentionPolicy.RUNTIME)
//public @interface Auditable {
//    AuditAction action();
//    String entityName() default "";
//    String description() default "";
//    boolean includeRequestBody() default false;
//    boolean includeResponseBody() default true;
//    String[] sensitiveFields() default {}; // Fields to mask in audit log
//}

import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    AuditAction action();
    String entityName() default "";
    String description() default "";
}
