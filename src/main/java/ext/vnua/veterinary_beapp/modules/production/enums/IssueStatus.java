package ext.vnua.veterinary_beapp.modules.production.enums;

public enum IssueStatus {
    PENDING("Chờ xử lý"),
    COMPLETED("Hoàn tất"),
    CANCELLED("Đã hủy");

    private final String description;

    IssueStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
