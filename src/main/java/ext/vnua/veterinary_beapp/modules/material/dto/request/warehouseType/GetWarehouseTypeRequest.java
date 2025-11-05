package ext.vnua.veterinary_beapp.modules.material.dto.request.warehouseType;

import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomWarehouseTypeQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetWarehouseTypeRequest extends CustomWarehouseTypeQuery.WareTypeFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;

    @Range(min = 3, max = 20, message = "Số lượng bản ghi trong một trang là từ 3 đến 20")
    private int limit = 3;
}
