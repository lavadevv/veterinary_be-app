package ext.vnua.veterinary_beapp.modules.material.dto.request.location;

import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomLocationQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetLocationRequest extends CustomLocationQuery.LocationFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;
    @Range(min = 5, max = 50, message = "Số lượng người dùng trong một trang là từ 5 đến 50 người")
    private int limit = 10;
}
