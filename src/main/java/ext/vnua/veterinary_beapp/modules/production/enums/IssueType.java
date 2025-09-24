package ext.vnua.veterinary_beapp.modules.production.enums;

public enum IssueType {
    MATERIAL("Nguyên liệu"),
    PACKAGING("Bao bì");

    private final String description;

    IssueType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
