package ext.vnua.veterinary_beapp.modules.productionplan.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Response chứa chi tiết nhu cầu nguyên vật liệu cho lệnh sản xuất
 */
@Data
public class MaterialRequirementDto {
    
    private Long formulaId;
    private String formulaCode;
    private String formulaName;
    private String formulaVersion;
    
    private BigDecimal batchSize;
    private String batchUnit;
    
    private List<MaterialItem> materials = new ArrayList<>();
    private Summary summary = new Summary();
    
    @Data
    public static class MaterialItem {
        private Integer orderNo;
        private Long materialId;
        private String materialCode;
        private String materialName;
        private String materialType;
        
        /**
         * Tỷ lệ % trong công thức
         */
        private BigDecimal percentage;
        
        /**
         * Số lượng cần dùng (đã tính theo batch size)
         */
        private BigDecimal requiredQuantity;
        
        /**
         * Đơn vị của số lượng (g, kg, l)
         */
        private String unit;
        
        /**
         * Đơn giá NVL (VNĐ/unit)
         */
        private BigDecimal unitPrice;
        
        /**
         * Thành tiền = requiredQuantity * unitPrice
         */
        private BigDecimal amount;
        
        /**
         * Ghi chú (nếu có từ formula item)
         */
        private String notes;
        
        /**
         * Có phải NVL quan trọng không
         */
        private Boolean isCritical;
    }
    
    @Data
    public static class Summary {
        /**
         * Tổng số lượng NVL (kg)
         */
        private BigDecimal totalQuantityKg;
        
        /**
         * Tổng giá trị (VNĐ)
         */
        private BigDecimal totalAmount;
        
        /**
         * Số lượng NVL khác nhau
         */
        private Integer materialCount;
    }
}
