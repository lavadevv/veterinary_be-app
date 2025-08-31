package ext.vnua.veterinary_beapp.modules.material.enums;

public enum TestStatus {
    CHO_KIEM_NGHIEM("Chờ kiểm nghiệm"),
    DAT("Đạt"),
    KHONG_DAT("Không đạt"),
    DANG_CACH_LY("Đang cách ly");

    private final String displayName;

    TestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
