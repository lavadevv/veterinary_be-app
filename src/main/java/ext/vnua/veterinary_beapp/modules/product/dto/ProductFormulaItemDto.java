package ext.vnua.veterinary_beapp.modules.product.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductFormulaItemDto {
    private Long id;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private BigDecimal quantity;   // định mức
    private String unit;
    private BigDecimal percentage; // %
    private Boolean isCritical;
    private String notes;

    @Override
    public String toString() {
        String code = (materialCode == null ? "" : materialCode);
        String name = (materialName == null ? "" : materialName);
        String unitStr = (unit == null ? "" : unit);
        String notesStr = (notes == null ? "" : notes);

        return "ProductFormulaItemDto{" +
                "id=" + id +
                ", materialId=" + materialId +
                ", materialCode='" + code + '\'' +
                ", materialName='" + name + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unitStr + '\'' +
                ", percentage=" + percentage +
                ", isCritical=" + isCritical +
                ", notes='" + notesStr + '\'' +
                '}';
    }

}