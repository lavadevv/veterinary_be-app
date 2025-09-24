package ext.vnua.veterinary_beapp.modules.production.dto.request.productionBatchRecord;

import ext.vnua.veterinary_beapp.modules.production.repository.custom.CustomProductionBatchRecordQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetProductionBatchRecordRequest extends CustomProductionBatchRecordQuery.ProductionBatchRecordFilterParam {
    @Min(value = 0, message = "Số trang phải >= 0")
    private int start = 0;

    @Range(min = 5, max = 50, message = "Số lượng trong 1 trang từ 5-50")
    private int limit = 10;
}
