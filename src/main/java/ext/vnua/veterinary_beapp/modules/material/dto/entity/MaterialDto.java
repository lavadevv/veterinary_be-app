package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import ext.vnua.veterinary_beapp.modules.material.enums.MaterialForm;
import ext.vnua.veterinary_beapp.modules.material.enums.MaterialType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaterialDto {
    private Long id;
    private String materialCode;
    private String materialName;
    private String shortName;
    private MaterialType materialType;
    private MaterialForm materialForm;
    private String activeIngredient;
    private BigDecimal purityPercentage;
    private BigDecimal iuPerGram;
    private String color;
    private String odor;
    private BigDecimal moistureContent;
    private BigDecimal viscosity;
    private String unitOfMeasure;
    private String standardApplied;
    private SupplierDto supplierDto;
    private BigDecimal minimumStockLevel;
    private BigDecimal currentStock;
    private BigDecimal fixedPrice;
    private Boolean requiresColdStorage;
    private String specialHandling;
    private Boolean isActive;
    private String notes;

    @Override
    public String toString() {
        String purityStr = purityPercentage != null ? purityPercentage.stripTrailingZeros().toPlainString() : "0";
        String currStr   = currentStock != null ? currentStock.stripTrailingZeros().toPlainString() : "0";
        String minStr    = minimumStockLevel != null ? minimumStockLevel.stripTrailingZeros().toPlainString() : "0";
        return "Vật tư:\n" +
                "   - ID: " + id + "\n" +
                "   - Mã: " + materialCode + "\n" +
                "   - Tên: " + materialName + "\n" +
                "   - Loại: " + (materialType != null ? materialType.name() : "Chưa xác định") + "\n" +
                "   - Dạng: " + (materialForm != null ? materialForm.name() : "Chưa xác định") + "\n" +
                "   - Hoạt chất: " + (activeIngredient != null ? activeIngredient : "") + "\n" +
                "   - Độ tinh khiết: " + purityStr + "%\n" +
                "   - Đơn vị tính: " + unitOfMeasure + "\n" +
                "   - Nhà cung cấp: " + (supplierDto != null ? supplierDto.getSupplierName() : "Không rõ") + "\n" +
                "   - Tồn kho hiện tại: " + currStr + "\n" +
                "   - Mức tồn kho tối thiểu: " + minStr + "\n" +
                "   - Trạng thái: " + (Boolean.TRUE.equals(isActive) ? "Đang sử dụng" : "Ngừng sử dụng") + "\n";
    }
}
