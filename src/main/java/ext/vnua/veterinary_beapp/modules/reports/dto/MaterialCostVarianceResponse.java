// response
package ext.vnua.veterinary_beapp.modules.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialCostVarianceResponse {
    private int year;
    private int month;
    private List<Row> items;
    private Totals totals;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Row {
        private LocalDate date;          // Ngày tháng (ngày nhận hoặc ngày phát sinh chi phí)
        private String code;             // Mã vật liệu / mã chi phí
        private String itemName;         // Mặt hàng / Tên chi phí
        private String uom;              // Đơn vị tính
        private BigDecimal quantity;     // Số lượng
        private BigDecimal unitPrice;    // Giá nhập (đơn giá theo lô / chi phí)
        private BigDecimal formulaPrice; // Giá công thức (có thể null)
        private BigDecimal variance;     // = formulaPrice - unitPrice (hoặc âm đối với other cost)
        private BigDecimal varianceAmount; // = variance * quantity
        private String notes;            // ghi chú
        private BigDecimal totalAmount;  // = unitPrice * quantity
        private String sourceType;       // MATERIAL_BATCH | OTHER_COST
        private Long sourceId;           // id bản ghi nguồn (để drilldown)
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Totals {
        private BigDecimal totalPurchaseAmount; // ∑ totalAmount
        private BigDecimal totalVarianceAmount; // ∑ varianceAmount
        private int lineCount;
    }
}
