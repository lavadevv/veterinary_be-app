package ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch;

import ext.vnua.veterinary_beapp.modules.product.repository.custom.CustomProductBatchQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;

@Data
public class GetProductBatchRequest extends CustomProductBatchQuery.ProductBatchFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;
    @Range(min = 5, max = 50, message = "Số lượng bản ghi trong một trang là từ 5 đến 50")
    private int limit = 10;

    // Kế thừa các filter field trong param:
    // Long productId; String batchNumber; ProductBatchStatus status;
    // LocalDate fromManufacturing; LocalDate toManufacturing; LocalDate toExpiry; ...
}