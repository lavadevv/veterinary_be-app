package ext.vnua.veterinary_beapp.modules.pcost.dto;

import ext.vnua.veterinary_beapp.modules.pcost.repository.custom.CustomProductionCostSheetQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetProductionCostSheets extends CustomProductionCostSheetQuery.ProductionCostSheetFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;   // chỉ số trang 0-based

    @Range(min = 5, max = 100, message = "Số lượng bản ghi trong một trang là từ 5 đến 100")
    private int limit = 20;  // số bản ghi mỗi trang
}
