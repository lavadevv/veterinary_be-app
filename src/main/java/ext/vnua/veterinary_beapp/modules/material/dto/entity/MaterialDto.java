package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import ext.vnua.veterinary_beapp.modules.material.enums.MaterialForm;
import ext.vnua.veterinary_beapp.modules.material.enums.MaterialType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MaterialDto {
    private Long id;
    private String materialCode;
    private String materialName;
    private String shortName;
    private MaterialType materialType;
    private MaterialForm materialForm;
    private String activeIngredient;
    private Double purityPercentage;
    private Double iuPerGram;
    private String color;
    private String odor;
    private Double moistureContent;
    private Double viscosity;
    private String unitOfMeasure;
    private String standardApplied;
    private SupplierDto supplierDto;
    private Double minimumStockLevel;
    private Double currentStock;
    private Double fixedPrice;
    private Boolean requiresColdStorage;
    private String specialHandling;
    private Boolean isActive;
    private String notes;

    @Override
    public String toString() {
        return String.format(
                "Vật tư:\n" +
                        "   - ID: %d\n" +
                        "   - Mã: %s\n" +
                        "   - Tên: %s\n" +
                        "   - Loại: %s\n" +
                        "   - Dạng: %s\n" +
                        "   - Hoạt chất: %s\n" +
                        "   - Độ tinh khiết: %.2f%%\n" +
                        "   - Đơn vị tính: %s\n" +
                        "   - Nhà cung cấp: %s\n" +
                        "   - Tồn kho hiện tại: %.2f\n" +
                        "   - Mức tồn kho tối thiểu: %.2f\n" +
                        "   - Trạng thái: %s\n",
                id,
                materialCode,
                materialName,
                materialType != null ? materialType.name() : "Chưa xác định",
                materialForm != null ? materialForm.name() : "Chưa xác định",
                activeIngredient,
                purityPercentage != null ? purityPercentage : 0.0,
                unitOfMeasure,
                supplierDto != null ? supplierDto.getSupplierName() : "Không rõ",
                currentStock != null ? currentStock : 0.0,
                minimumStockLevel != null ? minimumStockLevel : 0.0,
                Boolean.TRUE.equals(isActive) ? "Đang sử dụng" : "Ngừng sử dụng"
        );
    }

}
