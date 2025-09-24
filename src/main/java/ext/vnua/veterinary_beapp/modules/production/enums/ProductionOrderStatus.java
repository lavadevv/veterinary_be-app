package ext.vnua.veterinary_beapp.modules.production.enums;

public enum ProductionOrderStatus {
    PLANNED("Kế hoạch"),
    ISSUED("Đã phát hành"),
    IN_PROGRESS("Đang sản xuất"),
    QC_PENDING("Chờ QC"),
    COMPLETED("Hoàn thành"),
    CLOSED("Đã đóng"),
    CANCELLED("Đã hủy");

    private final String displayName;

    ProductionOrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
