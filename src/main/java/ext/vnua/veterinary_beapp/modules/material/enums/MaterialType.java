package ext.vnua.veterinary_beapp.modules.material.enums;

public enum MaterialType {
    NGUYEN_LIEU("Nguyên liệu"),
    PHU_LIEU("Phụ liệu"),
    BAO_BI("Bao bì"),
    VAT_TU_TIEU_HAO("Vật tư tiêu hao"),
    DUNG_MOI("Dung môi"),
    KHAC("Khác");

    private final String displayName;

    MaterialType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
