package ext.vnua.veterinary_beapp.modules.productionplan.enums;

import lombok.Getter;

/**
 * Trạng thái của kế hoạch sản xuất.
 * Giai đoạn đầu chỉ sử dụng PLANNING nhưng khai báo sẵn các trạng thái kế tiếp
 * để dễ mở rộng sang quy trình thực tế (phê duyệt, triển khai, hoàn tất, hủy).
 */
@Getter
public enum ProductionPlanStatus {
    PLANNING("Lên kế hoạch"),
    APPROVED("Đã phê duyệt"),
    SCHEDULED("Đã lên lịch"),
    IN_PROGRESS("Đang sản xuất"),
    COMPLETED("Đã hoàn tất"),
    CANCELLED("Đã hủy");

    private final String displayName;

    ProductionPlanStatus(String displayName) {
        this.displayName = displayName;
    }
}
