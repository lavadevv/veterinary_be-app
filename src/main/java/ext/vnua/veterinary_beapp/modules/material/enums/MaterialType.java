// src/main/java/ext/vnua/veterinary_beapp/modules/material/enums/MaterialType.java
package ext.vnua.veterinary_beapp.modules.material.enums;

import java.util.Arrays;

public enum MaterialType {
    // Nhóm mà FE đang dùng
    HOAT_CHAT("Hoạt chất"),
    NGUYEN_LIEU("Nguyên liệu"),
    PHU_LIEU("Phụ liệu"),
    BAO_BI("Bao bì"),
    CONG_CU("Công cụ"),
    THIET_BI("Thiết bị"),

    // Nhóm nghiệp vụ đang có ở BE (giữ để tương thích và báo cáo)
    DUNG_MOI("Dung môi"),
    VAT_TU_TIEU_HAO("Vật tư tiêu hao"),

    // Khác
    KHAC("Khác");

    private final String displayName;

    MaterialType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Tìm theo displayName (không phân biệt hoa thường, bỏ khoảng trắng dư) */
    public static MaterialType fromDisplayName(String name) {
        if (name == null) return null;
        String norm = name.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(t -> t.displayName.toLowerCase().equals(norm))
                .findFirst()
                .orElse(null);
    }

    /** Tìm theo tên enum (ví dụ "HOAT_CHAT" hoặc "NGUYEN_LIEU") */
    public static MaterialType fromCode(String code) {
        if (code == null) return null;
        try {
            return MaterialType.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
