package ext.vnua.veterinary_beapp.modules.pricing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class BrandPriceRowDto {
    private Integer stt;               // STT
    private String manualCode;         // Mã chi phí (người dùng điền)
    private BigDecimal packSize;       // Kích cỡ đóng gói (ml/g)
    private String specName;           // Tên quy cách

    private BigDecimal cpnl;           // Chi phí nguyên liệu / 1 sp
    private BigDecimal cpsx;           // Chi phí sản xuất / 1 sp (unitCost)
    private BigDecimal cost;           // cpnl + cpsx
    private BigDecimal profitPercent;  // % LN (0.09 = 9%)
    private BigDecimal salePrice;      // cost * (1 + %LN)
}
