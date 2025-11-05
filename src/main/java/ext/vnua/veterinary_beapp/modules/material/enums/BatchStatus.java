package ext.vnua.veterinary_beapp.modules.material.enums;

/**
 * Status of a MaterialBatch container
 * Represents the lifecycle stage of a batch shipment
 */
public enum BatchStatus {
    ACTIVE("Đang hoạt động"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã hủy");

    private final String displayName;

    BatchStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
