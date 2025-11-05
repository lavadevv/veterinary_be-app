// File: ext/vnua/veterinary_beapp/modules/material/dto/entity/MaterialDto.java
package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MaterialDto {
    private Long id;

    private String materialCode;
    private String materialName;
    private String internationalName;

    /** Thay cho enum MaterialType cũ */
    private Long materialCategoryId;
    private String materialCategoryName;

    /** Thay cho enum MaterialForm cũ */
    private Long materialFormTypeId;
    private String materialFormTypeName;

    /** Danh sách hoạt chất (nếu có) — map từ Material.activeIngredients */
    private List<ActiveIngredientLine> activeIngredients;
    
    /** Số lượng hoạt chất - để FE hiển thị nhanh mà không cần đếm array */
    private Integer activeIngredientsCount;

    private BigDecimal purityPercentage;   // 0..100 (%)
    private BigDecimal iuPerGram;          // IU/gram (nếu có dược lực)
    private String color;
    private String odor;
    private BigDecimal moistureContent;    // 0..100 (%)
    private BigDecimal viscosity;

    /** UoM hiển thị đẹp hơn thay vì chỉ mỗi id */
    private Long unitOfMeasureId;
    private String unitOfMeasureName;
    private String unitOfMeasureSymbol;

    private String standardApplied;

    private SupplierDto supplierDto;

    private BigDecimal minimumStockLevel;
    private BigDecimal currentStock;       // dẫn xuất từ batches
    private BigDecimal fixedPrice;

    private Boolean requiresColdStorage;
    private String specialHandling;
    private Boolean isActive;
    private String notes;

    /** Dòng hoạt chất gọn nhẹ cho FE */
    @Data
    public static class ActiveIngredientLine {
        private Long ingredientId;
        private String ingredientName;
        private BigDecimal contentValue;   // ví dụ: 50.0
        private String contentUnit;        // ví dụ: "%", "mg/g", "IU/g"
    }

    @Override
    public String toString() {
        String purityStr = purityPercentage != null ? purityPercentage.stripTrailingZeros().toPlainString() : "0";
        String currStr   = currentStock != null ? currentStock.stripTrailingZeros().toPlainString() : "0";
        String minStr    = minimumStockLevel != null ? minimumStockLevel.stripTrailingZeros().toPlainString() : "0";

        String cat = materialCategoryName != null ? materialCategoryName : "Chưa phân loại";
        String form = materialFormTypeName != null ? materialFormTypeName : "Chưa xác định";
        String uom = unitOfMeasureName != null ? unitOfMeasureName : (unitOfMeasureId != null ? ("#" + unitOfMeasureId) : "N/A");
        String sup = (supplierDto != null ? supplierDto.getSupplierName() : "Không rõ");

        return "Vật tư:\n" +
                "   - ID: " + id + "\n" +
                "   - Mã: " + materialCode + "\n" +
                "   - Tên: " + materialName + "\n" +
                "   - Nhóm/Loại: " + cat + "\n" +
                "   - Dạng: " + form + "\n" +
                "   - Độ tinh khiết: " + purityStr + "%\n" +
                "   - Đơn vị tính: " + uom + "\n" +
                "   - Nhà cung cấp: " + sup + "\n" +
                "   - Tồn kho hiện tại: " + currStr + "\n" +
                "   - Mức tồn kho tối thiểu: " + minStr + "\n" +
                "   - Trạng thái: " + (Boolean.TRUE.equals(isActive) ? "Đang sử dụng" : "Ngừng sử dụng") + "\n";
    }
}
