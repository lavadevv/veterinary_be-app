package ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
public class UpsertFormulaRequest {

    /* ===== Header / Catalog level (bắt buộc & tuỳ chọn) ===== */

    /** MÃ CÔNG THỨC — bắt buộc, xác định Header (công thức dùng chung) */
    @NotBlank
    private String formulaCode;

    /** Tên công thức — optional (chỉ dùng khi tạo mới header) */
    private String formulaName;

    /** Mô tả header — optional */
    private String headerDescription;

    /** Danh sách sản phẩm áp dụng — optional (gán header ↔ products) */
    private Set<Long> productIds;

    /** Ghi chú thay đổi/phiên bản — optional */
    private String changeNote;

    /* ===== Version level (phiên bản công thức) ===== */

    /** Version — optional; nếu null service sẽ auto yyyyMMdd-HHmmss */
    private String version;

    /** Kích thước lô (nếu công thức dùng định mức tuyệt đối). Có thể null */
    private BigDecimal batchSize;

    private String description;
    private String sopFilePath;

    /** Đánh dấu active cho phiên bản mới tạo */
    private Boolean isActive = true;

    /** Công thức dung dịch? (true → có thể cho phép tổng % > 100) */
    private Boolean isLiquidFormula;

    /** “Per …” – cơ sở tính hàm lượng công thức. Mặc định 1000 g (tức per 1 kg) */
    private BigDecimal basisValue;          // default 1000
    private String basisUnit;               // "g" | "kg" | "l"  (default "g")

    /** Mật độ (g/mL) – chỉ dùng khi basisUnit="l". Optional */
    private BigDecimal density;

    /* ===== Items (bắt buộc) ===== */

    @Valid
    @NotNull
    private List<FormulaItem> items;

    @Data
    public static class FormulaItem {
        @NotNull
        private Long materialId;

        // Khai báo tuyệt đối (có thể bỏ trống nếu dùng %)
        private BigDecimal quantity;
        private String unit;                 // g|kg (nếu dùng quantity)

        // Khai báo theo %
        @DecimalMin(value = "0.0000")
        private BigDecimal percentage;       // optional

        private Boolean isCritical = false;
        private String notes;

        /* ===== Label (optional, để tính % đạt) ===== */
        private BigDecimal labelAmount;      // ví dụ 30000
        private String labelUnit;            // "mg" | "g" | "kg"

        /* ===== NEW: Active Ingredients tracking (optional) ===== */
        /**
         * Danh sách hoạt chất của material này với labelAmount cho từng hoạt chất
         * Frontend sẽ gửi lên khi user nhập label amount cho từng active ingredient
         */
        @Valid
        private List<ActiveIngredientItem> activeIngredients;
    }

    @Data
    public static class ActiveIngredientItem {
        /** ID của ActiveIngredient (từ MaterialActiveIngredient) */
        private Long activeIngredientId;

        /** Hàm lượng nhãn cho hoạt chất này (user input) */
        private BigDecimal labelAmount;

        /** Đơn vị hàm lượng nhãn */
        private String labelUnit;            // "mg" | "g" | "kg" | "IU"

        /** Ghi chú riêng cho hoạt chất này */
        private String notes;
    }
}
