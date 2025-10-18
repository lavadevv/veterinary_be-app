package ext.vnua.veterinary_beapp.modules.pricing.dto;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PricingCalculateRequest {
    /** ID công thức để tính CPNL */
    @NotNull
    private Long formulaId;

    /** Batch size theo Kg (ví dụ 100) */
    @NotNull @Positive
    private BigDecimal batchSizeKg;

    /** Danh sách brand/nhà SX cần tính */
    @NotNull
    @Valid
    private List<BrandInput> brands;

    @Data
    public static class BrandInput {
        /** Mã chi phí do người dùng điền (hiển thị ở cột đầu) */
        private String manualCode;

        /** Mã sheet CPSX để truy vấn (ví dụ: "50ml-Diclacox-DY") */
        @NotNull
        private String sheetCode;

        /** "Kích cỡ đóng gói" (ml hoặc g), ví dụ 50 */
        @NotNull @Positive
        private BigDecimal packSize;

        /** Tên quy cách hiển thị */
        @NotNull
        private String specName;

        /** % lợi nhuận dạng THẬP PHÂN (0.09 = 9%) */
        @NotNull
        private BigDecimal profitPercent;
    }
}
