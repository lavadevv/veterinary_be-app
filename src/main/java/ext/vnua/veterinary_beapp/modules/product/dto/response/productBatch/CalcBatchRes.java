package ext.vnua.veterinary_beapp.modules.product.dto.response.productBatch;

import java.math.BigDecimal;
import java.util.List;

public record CalcBatchRes(
        Long productId,
        Long formulaId,
        BigDecimal batchSizeKg,
        List<Line> items,
        Totals totals
) {
    public record Line(
            Integer orderNo,
            Long materialId,
            String materialCode,
            String materialName,
            String uom,                    // luôn "g" cho qtyG
            BigDecimal percentage,         // 0.3, 20.0, ...
            BigDecimal qtyG,               // đã làm tròn 3 số
            BigDecimal unitPricePerG,      // đã quy đổi theo UOM
            BigDecimal amount              // làm tròn đến đồng
    ) { }

    public record Totals(
            BigDecimal qtyKg,
            BigDecimal amount
    ) { }
}
