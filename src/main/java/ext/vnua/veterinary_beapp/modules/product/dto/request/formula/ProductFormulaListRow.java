package ext.vnua.veterinary_beapp.modules.product.dto.request.formula;

import ext.vnua.veterinary_beapp.modules.product.enums.FormulationType;
import ext.vnua.veterinary_beapp.modules.product.enums.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProductFormulaListRow {
    private Long id;
    // Header info (để FE hiển thị đúng tên/mã công thức)
    private String formulaCode;
    private String formulaName;

    private Long productId;
    private String productCode;
    private String productName;

    private String version;
    private Boolean isActive;
    private BigDecimal batchSize;

    private Long totalItems;     // số dòng NVL
    private Long criticalItems;  // số dòng NVL quan trọng

    private LocalDateTime createdDate;
    private String createdBy;

    // thêm 2 trường để filter/hiển thị
    private ProductCategory productCategory;
    private FormulationType formulationType;
}
