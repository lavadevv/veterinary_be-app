package ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompleteBatchRequest {
    @NotNull private Long batchId;
    @NotNull private BigDecimal actualQuantity;    // thành phẩm nhập kho
    private BigDecimal rejectedQuantity;           // phế phẩm (nếu có)
    private String qcCertificatePath;              // tệp QC (nếu có)
    @NotNull
    private Long locationId;         // vị trí nhập kho
    private String notes;
}