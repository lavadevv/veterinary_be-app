package ext.vnua.veterinary_beapp.modules.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductFormulaDto {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private String version;
    private BigDecimal batchSize;
    private String description;
    private Boolean isActive;
    private String createdBy;
    private Long approvedById;
    private String sopFilePath;
    private List<ProductFormulaItemDto> formulaItems;

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
                ", items=" + itemCount +
                '}';
    }

}