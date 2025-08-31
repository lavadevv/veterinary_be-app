package ext.vnua.veterinary_beapp.modules.material.dto.request.supplier;

import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomSupplierQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetSupplierRequest extends CustomSupplierQuery.SupplierFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;
    @Range(min = 5, max = 50, message = "Số lượng nhà cung cấp trong một trang là từ 5 đến 50")
    private int limit = 10;
}
