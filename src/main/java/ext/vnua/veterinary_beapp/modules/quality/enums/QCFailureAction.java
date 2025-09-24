package ext.vnua.veterinary_beapp.modules.quality.enums;

import lombok.Getter;

@Getter
public enum QCFailureAction {
    ISOLATE("Cách ly"),
    REWORK("Tái sản xuất"),
    SCRAP("Hủy bỏ"),
    INVESTIGATE("Điều tra"),
    APPROVE_DEVIATION("Chấp nhận lệch chuẩn");

    private final String description;
    QCFailureAction(String description) { this.description = description; }
}
