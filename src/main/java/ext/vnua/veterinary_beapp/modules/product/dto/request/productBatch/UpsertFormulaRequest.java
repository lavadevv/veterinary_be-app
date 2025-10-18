package ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpsertFormulaRequest {
    @NotNull private Long productId;

    @NotBlank private String version;      // v1.0, v1.1...

    /** Kích thước lô (nếu công thức dùng định mức tuyệt đối). Có thể null */
    private BigDecimal batchSize;

    private String description;
    private String sopFilePath;
    private Boolean isActive = true;

    /** NEW: Công thức dung dịch? (true → có thể cho phép tổng % > 100) */
    private Boolean isLiquidFormula;

    /** NEW: “Per …” – cơ sở tính hàm lượng công thức. Mặc định 1000 g (tức per 1 kg) */
    private BigDecimal basisValue;          // default 1000
    private String basisUnit;               // "g" | "kg" | "l"  (default "g")

    /** NEW: Mật độ (g/mL) – chỉ dùng khi basisUnit="l". Optional */
    private BigDecimal density;

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

        /* ===== NEW: label (optional) ===== */
        private BigDecimal labelAmount;      // ví dụ 30_000
        private String labelUnit;            // ví dụ "mg"
    }
}
