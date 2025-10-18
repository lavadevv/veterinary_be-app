package ext.vnua.veterinary_beapp.modules.pricing.ports;

import java.math.BigDecimal;

public interface ProductionCostSheetPort {
    /**
     * Lấy đơn giá CPSX / 1 đơn vị theo mã sheet.
     */
    BigDecimal getUnitCostBySheetCode(String sheetCode);
}
