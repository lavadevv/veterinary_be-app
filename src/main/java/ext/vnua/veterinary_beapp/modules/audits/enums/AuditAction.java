package ext.vnua.veterinary_beapp.modules.audits.enums;

public enum AuditAction {
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    LOGIN("LOGIN"),
    LOGOUT("LOGOUT"),
    VIEW("VIEW"),
    EXPORT("EXPORT"),
    IMPORT("IMPORT");

    private final String value;

    AuditAction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
