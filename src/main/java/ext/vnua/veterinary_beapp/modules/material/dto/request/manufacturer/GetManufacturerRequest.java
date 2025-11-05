package ext.vnua.veterinary_beapp.modules.material.dto.request.manufacturer;

import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomManufacturerQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetManufacturerRequest extends CustomManufacturerQuery.ManuFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;

    @Range(min = 3, max = 200, message = "Số lượng bản ghi trong một trang là từ 3 đến 200")
    private int limit = 20;
}
