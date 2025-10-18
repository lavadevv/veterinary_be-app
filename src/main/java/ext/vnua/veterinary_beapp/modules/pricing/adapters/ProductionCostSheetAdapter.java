package ext.vnua.veterinary_beapp.modules.pricing.adapters;

import ext.vnua.veterinary_beapp.modules.pcost.dto.ProductionCostSheetDto;
import ext.vnua.veterinary_beapp.modules.pcost.service.ProductionCostSheetService;
import ext.vnua.veterinary_beapp.modules.pricing.ports.ProductionCostSheetPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Adapter gọi trực tiếp ProductionCostSheetService.getByCode(...) để lấy unitCost.
 */
@Component
@RequiredArgsConstructor
public class ProductionCostSheetAdapter implements ProductionCostSheetPort {

    private final ProductionCostSheetService productionCostSheetService;

    @Override
    public BigDecimal getUnitCostBySheetCode(String sheetCode) {
        ProductionCostSheetDto dto = productionCostSheetService.getByCode(sheetCode);
        if (dto == null || dto.getUnitCost() == null) {
            throw new IllegalStateException("Không lấy được unitCost cho sheetCode: " + sheetCode);
        }
        return dto.getUnitCost();
    }
}
