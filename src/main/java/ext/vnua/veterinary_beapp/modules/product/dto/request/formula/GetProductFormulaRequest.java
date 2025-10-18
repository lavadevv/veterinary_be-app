package ext.vnua.veterinary_beapp.modules.product.dto.request.formula;

import ext.vnua.veterinary_beapp.modules.product.repository.custom.CustomProductFormulaQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;


/** Request lấy danh sách công thức (filter + paging) */
@Data
public class GetProductFormulaRequest extends CustomProductFormulaQuery.ProductFormulaFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;

    @Range(min = 5, max = 100, message = "Số lượng bản ghi trong một trang là từ 5 đến 100")
    private int limit = 20;
    // Kế thừa các field filter:
    // Long productId; String productCode; String productName; String version; Boolean active;
    // LocalDate fromCreatedDate; LocalDate toCreatedDate; String keywords; String productLabel;
    // String sortField; String sortType;
}
