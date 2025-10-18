package ext.vnua.veterinary_beapp.modules.pricing.adapters;

import ext.vnua.veterinary_beapp.modules.pricing.ports.ProductBatchCalcPort;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.CalcBatchReq;
import ext.vnua.veterinary_beapp.modules.product.dto.response.productBatch.CalcBatchRes;
import ext.vnua.veterinary_beapp.modules.product.services.ProductBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Adapter gọi trực tiếp ProductBatchService.calc(...) để lấy totals (amount, qtyKg).
 */
@Component
@RequiredArgsConstructor
public class ProductBatchCalcAdapter implements ProductBatchCalcPort {

    private final ProductBatchService productBatchService;

    @Override
    public CalcSummary calculateTotals(Long formulaId, BigDecimal batchSizeKg) {
        CalcBatchReq req = new CalcBatchReq(formulaId, batchSizeKg);
        CalcBatchRes res = productBatchService.calc(req);
        if (res == null || res.totals() == null || res.totals().amount() == null || res.totals().qtyKg() == null) {
            throw new IllegalStateException("calc() không trả đủ dữ liệu totals (amount/qtyKg).");
        }
        return new CalcSummary(res.totals().amount(), res.totals().qtyKg());
        // res.totals().amount(): tổng tiền NVL cho cả batch
        // res.totals().qtyKg(): khối lượng batch (kg)
    }
}
