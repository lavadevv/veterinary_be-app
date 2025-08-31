package ext.vnua.veterinary_beapp.modules.material.dto.request.warehouse;

import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomWarehouseQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.PageRequest;

@Data
public class GetWarehouseRequest extends CustomWarehouseQuery.WarehouseFilterParam{
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;
    @Range(min = 5, max = 50, message = "Số lượng phần tử trong một trang là từ 5 đến 50")
    private int limit = 10;
}
