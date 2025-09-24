package ext.vnua.veterinary_beapp.modules.production.dto.request;

import ext.vnua.veterinary_beapp.modules.production.repository.custom.CustomProductionOrderQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetProductionOrderRequest extends CustomProductionOrderQuery.ProductionOrderFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;
    @Range(min = 5, max = 50, message = "Số lượng trong một trang là từ 5 đến 50")
    private int limit = 10;
}
