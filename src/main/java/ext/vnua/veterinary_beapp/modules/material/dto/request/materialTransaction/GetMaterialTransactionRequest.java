package ext.vnua.veterinary_beapp.modules.material.dto.request.materialTransaction;

import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialTransactionQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetMaterialTransactionRequest extends CustomMaterialTransactionQuery.MaterialTransactionFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;
    @Range(min = 5, max = 50, message = "Số lượng bản ghi trong một trang là từ 5 đến 50")
    private int limit = 10;
}

