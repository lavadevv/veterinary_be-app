package ext.vnua.veterinary_beapp.modules.product.enums;

public enum FormulationType {
    BOT_UONG("Bột uống"),
    DUNG_DICH("Dung dịch"),
    VIEN("Viên"),
    TIEM("Tiêm"),
    HON_DICH("Hỗn dịch"),
    GEL("Gel"),
    CAPSULE("Capsule"),
    CREAM("Kem"),
    SPRAY("Xịt"),
    DROPS("Nhỏ giọt");

    private final String displayName;

    FormulationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
