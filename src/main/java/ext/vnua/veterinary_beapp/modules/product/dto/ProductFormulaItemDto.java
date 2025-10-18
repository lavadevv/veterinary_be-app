package ext.vnua.veterinary_beapp.modules.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductFormulaItemDto {
    private Long id;
    private Long materialId;
    private String materialCode;
    private String materialName;

    // Khai báo theo định mức tuyệt đối (tuỳ công thức)
    private BigDecimal quantity;   // định mức
    private String unit;           // g|kg (như hiện tại)

    // Khai báo theo %
    private BigDecimal percentage; // %

    private Boolean isCritical;
    private String notes;

    /* ================== BỔ SUNG CHO BÀI TOÁN HÀM LƯỢNG ================== */

    /** (OPTIONAL) Hàm lượng theo NHÃN – do người dùng nhập. Có thể null. */
    private BigDecimal labelAmount;      // ví dụ: 30_000
    private String labelUnit;            // ví dụ: mg | g | kg | IU

    /** (CALC) Hàm lượng công thức BE tính (từ purity% * khối lượng NVL), quy đổi theo đơn vị dưới. */
    private BigDecimal formulaContentAmount;
    private String formulaContentUnit;   // ví dụ: mg | g | kg | IU

    /** (CALC) % đạt = formulaContent / (labelAmount / 100), làm tròn 1 số.
     *  Nếu không có labelAmount/labelUnit -> trả null.
     */
    private BigDecimal achievedPercent;

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
                ", labelAmount=" + labelAmount +
                ", labelUnit='" + labelUnit + '\'' +
                ", formulaContentAmount=" + formulaContentAmount +
                ", formulaContentUnit='" + formulaContentUnit + '\'' +
                ", achievedPercent=" + achievedPercent +
                '}';
    }
}
