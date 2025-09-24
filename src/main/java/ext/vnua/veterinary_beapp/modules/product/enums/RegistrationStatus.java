package ext.vnua.veterinary_beapp.modules.product.enums;

public enum RegistrationStatus {
    ACTIVE("Đang hiệu lực"),
    EXPIRED("Hết hạn"),
    SUSPENDED("Tạm ngừng"),
    REVOKED("Thu hồi"),
    PENDING_RENEWAL("Chờ gia hạn");

    private final String displayName;

    RegistrationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}