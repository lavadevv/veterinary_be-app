package ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch;

import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialBatchQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetMaterialBatchRequest extends CustomMaterialBatchQuery.MaterialBatchFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;

    @Range(min = 5, max = 50, message = "Số lượng bản ghi trong một trang là từ 5 đến 50")
    private int limit = 10;
}
