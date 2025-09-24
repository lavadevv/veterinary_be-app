package ext.vnua.veterinary_beapp.modules.quality.enums;

import lombok.Getter;

@Getter
public enum QCResult {
    PASS("Đạt"),
    FAIL("Không đạt"),
    PENDING("Chưa có kết quả"),
    RETEST_REQUIRED("Cần kiểm tra lại");

    private final String description;
    QCResult(String description) { this.description = description; }
}
