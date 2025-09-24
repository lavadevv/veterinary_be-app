package ext.vnua.veterinary_beapp.modules.quality.enums;

import lombok.Getter;

@Getter
public enum QCStatus {
    PENDING("Chờ kiểm tra"),
    IN_REVIEW("Đang xem xét"),
    APPROVED("Đã phê duyệt"),
    REJECTED("Bị từ chối"),
    RETEST("Yêu cầu kiểm tra lại");

    private final String description;
    QCStatus(String description) { this.description = description; }
}
