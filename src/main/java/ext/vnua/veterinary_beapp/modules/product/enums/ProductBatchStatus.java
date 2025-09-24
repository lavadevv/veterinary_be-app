package ext.vnua.veterinary_beapp.modules.product.enums;

public enum ProductBatchStatus {
    IN_PROGRESS("Đang sản xuất"),
    QC_PENDING("Chờ kiểm nghiệm"),
    APPROVED("Đạt"),
    REJECTED("Không đạt"),
    QUARANTINE("Cách ly"),
    RELEASED("Xuất bán"),
    EXPIRED("Hết hạn"),
    STORED("Đã nhập kho"),       // thêm
    CLOSED("Đã đóng lô");

    private final String displayName;

    ProductBatchStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
