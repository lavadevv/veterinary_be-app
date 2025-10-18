package ext.vnua.veterinary_beapp.modules.pricing.ports;

import java.math.BigDecimal;

public interface ProductBatchCalcPort {

    /**
     * Tính tổng tiền nguyên liệu cho batch (dựa trên formulaId & batchSizeKg).
     * @return wrapper chỉ cần 2 giá trị: totalAmount, qtyKg
     */
    CalcSummary calculateTotals(Long formulaId, BigDecimal batchSizeKg);

    class CalcSummary {
        private final BigDecimal totalAmount; // tổng tiền NVL của batch
        private final BigDecimal qtyKg;       // khối lượng batch (kg)

        public CalcSummary(BigDecimal totalAmount, BigDecimal qtyKg) {
            this.totalAmount = totalAmount;
            this.qtyKg = qtyKg;
        }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getQtyKg() { return qtyKg; }
    }
}
