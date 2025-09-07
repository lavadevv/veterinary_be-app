package ext.vnua.veterinary_beapp.modules.material.dto.request.material;

import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetMaterialRequest extends CustomMaterialQuery.MaterialFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;
    @Range(min = 5, max = 100, message = "Số lượng bản ghi trong một trang là từ 5 đến 100")
    private int limit = 10;
}
