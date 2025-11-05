// ext/vnua/veterinary_beapp/modules/material/dto/request/uom/GetUnitOfMeasureRequest.java
package ext.vnua.veterinary_beapp.modules.material.dto.request.uom;

import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomUnitOfMeasureQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetUnitOfMeasureRequest extends CustomUnitOfMeasureQuery.UomFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;

    @Range(min = 3, max = 50, message = "Số lượng bản ghi một trang từ 3 đến 50")
    private int limit = 10;
}
