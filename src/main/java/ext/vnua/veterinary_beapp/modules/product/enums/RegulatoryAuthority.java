package ext.vnua.veterinary_beapp.modules.product.enums;

public enum RegulatoryAuthority {
    CUC_THU_Y("Cục Thú y"),
    CUC_ATTP("Cục An toàn Thực phẩm"),
    BO_NN("Bộ Nông nghiệp và Phát triển nông thôn"),
    OTHER("Khác");

    private final String displayName;

    RegulatoryAuthority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
