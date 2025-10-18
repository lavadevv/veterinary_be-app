package ext.vnua.veterinary_beapp.modules.pcost.dto;

import ext.vnua.veterinary_beapp.modules.pcost.repository.custom.CustomLaborRateQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetLaborRate extends CustomLaborRateQuery.LaborRateFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;

    @Range(min = 5, max = 50, message = "Số lượng bản ghi trong một trang là từ 5 đến 50")
    private int limit = 10;
}
