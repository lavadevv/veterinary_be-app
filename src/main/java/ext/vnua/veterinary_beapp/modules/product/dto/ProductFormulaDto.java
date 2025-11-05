package ext.vnua.veterinary_beapp.modules.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductFormulaDto {
    private Long id;
    
    // ===== Header/Catalog level fields (from FormulaHeader) =====
    private String formulaCode;           // Mã công thức (từ Header)
    private String formulaName;           // Tên công thức (từ Header)
    private String headerDescription;     // Mô tả chung (từ Header)
    private List<AppliedProduct> appliedProducts; // Danh sách sản phẩm áp dụng
    
    // ===== Representative Product (first product for backward compatibility) =====
    private Long productId;               // ID sản phẩm đại diện (first product)
    private String productCode;           // Mã sản phẩm đại diện
    private String productName;           // Tên sản phẩm đại diện
    
    // ===== Version level fields =====
    private String version;
    private BigDecimal batchSize;
    private String description;
    private Boolean isActive;
    private String createdBy;
    private Long approvedById;
    private String sopFilePath;
    
    // Audit fields (optional)
    private java.time.LocalDateTime createdDate;
    private java.time.LocalDateTime lastModifiedDate;
    private String lastModifiedBy;

    /** NEW: cờ công thức dung dịch (cho phép tổng % > 100) */
    private Boolean isLiquidFormula;

    private List<ProductFormulaItemDto> formulaItems;
    
    // ===== Nested DTO for applied products =====
    @Data
    public static class AppliedProduct {
        private Long id;
        private String productCode;
        private String productName;
    }

    @Override
    public String toString() {
        int itemCount = (formulaItems == null ? 0 : formulaItems.size());
        String ver = (version == null ? "" : version);
        String pCode = (productCode == null ? "" : productCode);
        String pName = (productName == null ? "" : productName);
        String sop = (sopFilePath == null ? "" : sopFilePath);
        String desc = (description == null ? "" : description);

        return "ProductFormulaDto{" +
                "id=" + id +
                ", productId=" + productId +
                ", productCode='" + pCode + '\'' +
                ", productName='" + pName + '\'' +
                ", version='" + ver + '\'' +
                ", batchSize=" + batchSize +
                ", description='" + desc + '\'' +
                ", isActive=" + isActive +
                ", createdById=" + createdBy +
                ", approvedById=" + approvedById +
                ", sopFilePath='" + sop + '\'' +
                ", isLiquidFormula=" + isLiquidFormula +
                ", items=" + itemCount +
                '}';
    }
}
