package ext.vnua.veterinary_beapp.modules.material.enums;

public enum MaterialForm {
    BOT("Bột"),
    LONG("Lỏng"),
    HAT("Hạt"),
    VIEN("Viên"),
    BAO_PHIM("Bao phim"),
    DUNG_DICH("Dung dịch"),
    NHUA("Nhựa"),
    KHAC("Khác");

    private final String displayName;

    MaterialForm(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
