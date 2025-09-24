package ext.vnua.veterinary_beapp.modules.production.dto.request.productionOrderIssue;

import ext.vnua.veterinary_beapp.modules.production.repository.custom.CustomProductionOrderIssueQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetProductionOrderIssueRequest extends CustomProductionOrderIssueQuery.ProductionOrderIssueFilterParam {

    @Min(value = 0, message = "Số trang phải >= 0")
    private int start = 0;

    @Range(min = 5, max = 50, message = "Số lượng trong 1 trang từ 5-50")
    private int limit = 10;
}
