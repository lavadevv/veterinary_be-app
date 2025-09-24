package ext.vnua.veterinary_beapp.modules.quality.enums;


import lombok.Getter;

@Getter
public enum QCType {
    RAW_MATERIAL("Kiểm tra nguyên liệu"),
    IN_PROCESS("Kiểm tra trong quá trình"),
    FINAL("Kiểm tra thành phẩm"),
    SAMPLING("Kiểm tra lấy mẫu định kỳ"),
    PACKAGING("Kiểm tra bao bì"),
    STABILITY("Kiểm tra độ ổn định");

    private final String description;
    QCType(String description) { this.description = description; }
}