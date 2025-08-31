package ext.vnua.veterinary_beapp.modules.material.enums;

public enum UsageStatus {
    CACH_LY("Cách ly"),
    SAN_SANG_SU_DUNG("Sẵn sàng sử dụng"),
    DANG_SU_DUNG("Đang sử dụng"),
    DA_HET("Đã hết"),
    BI_CAM("Bị cấm"),
    HET_HAN("Hết hạn"),
    HONG("Hỏng");

    private final String displayName;

    UsageStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
