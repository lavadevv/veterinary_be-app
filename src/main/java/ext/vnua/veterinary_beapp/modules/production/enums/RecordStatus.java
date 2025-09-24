package ext.vnua.veterinary_beapp.modules.production.enums;

public enum RecordStatus {
    PENDING("PENDING", "Chờ duyệt"),
    APPROVED("APPROVED", "Đã duyệt"),
    REJECTED("REJECTED", "Từ chối"),
    IN_REVIEW("IN_REVIEW", "Đang xem xét");

    private final String code;
    private final String description;

    RecordStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}