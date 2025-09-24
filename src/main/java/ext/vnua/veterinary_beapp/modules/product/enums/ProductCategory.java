package ext.vnua.veterinary_beapp.modules.product.enums;

public enum ProductCategory {
    THANH_PHAM("Thành phẩm"), // sản phẩm hoàn thiện, đóng gói đủ, sẵn sàng bán
    BAN_THANH_PHAM("Bán thành phẩm"); // sản phẩm chưa hoàn thiện đóng gói hoặc chưa kiểm nghiệm cuối

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
