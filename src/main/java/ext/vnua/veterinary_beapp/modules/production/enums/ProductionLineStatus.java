package ext.vnua.veterinary_beapp.modules.production.enums;

import lombok.Getter;

@Getter
public enum ProductionLineStatus {
    ACTIVE("ACTIVE", "Hoạt động"),
    INACTIVE("INACTIVE", "Không hoạt động"),
    UNDER_MAINTENANCE("UNDER_MAINTENANCE", "Đang bảo trì");

    private final String code;
    private final String description;

    ProductionLineStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ProductionLineStatus fromCode(String code) {
        for (ProductionLineStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid production line status: " + code);
    }

    public static boolean isValid(String code) {
        try {
            fromCode(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}