package ext.vnua.veterinary_beapp.modules.product.enums;

public enum ProductBatchStatus {
    DRAFT("Nháp"),                 // <— thêm: lô vừa tạo từ công thức, chưa issue NVL
    IN_PROGRESS("Đang sản xuất"),  // đã issue (reserve NVL)
    QC_PENDING("Chờ kiểm nghiệm"),
    APPROVED("Đạt"),
    REJECTED("Không đạt"),
    QUARANTINE("Cách ly"),
    RELEASED("Xuất bán"),
    EXPIRED("Hết hạn"),
    STORED("Đã nhập kho"),
    CLOSED("Đã đóng lô"),
    CANCELED("Đã hủy");            // <— thêm: unissue, đã xả NVL

    private final String displayName;
    ProductBatchStatus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
