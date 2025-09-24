package ext.vnua.veterinary_beapp.modules.quality.enums;

import lombok.Getter;

@Getter
public enum ParameterType {
    NUMERIC("Giá trị số"),
    TEXT("Văn bản"),
    BOOLEAN("Có/Không"),
    OPTION("Lựa chọn");

    private final String description;
    ParameterType(String description) { this.description = description; }
}
