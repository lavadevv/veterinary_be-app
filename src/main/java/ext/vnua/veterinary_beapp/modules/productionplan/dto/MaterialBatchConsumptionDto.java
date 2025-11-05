package ext.vnua.veterinary_beapp.modules.productionplan.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Response cho simulation tiêu hao nguyên vật liệu theo FIFO
 * Dùng cho "Lệnh xuất nguyên liệu"
 */
@Data
public class MaterialBatchConsumptionDto {
    
    private Long formulaId;
    private String formulaCode;
    private String formulaName;
    private String formulaVersion;
    
    private BigDecimal batchSize;
    private String batchUnit;
    
    private Long lotId;
    private String lotNumber;
    
    private List<MaterialConsumption> materials = new ArrayList<>();
    private ConsumptionSummary summary = new ConsumptionSummary();
    
    /**
     * Thông tin tiêu hao cho một nguyên vật liệu
     */
    @Data
    public static class MaterialConsumption {
        private Long materialId;
        private String materialCode;
        private String materialName;
        private String materialType;
        
        /**
         * Tổng số lượng cần dùng (đã tính theo công thức)
         */
        private BigDecimal requiredQuantity;
        
        /**
         * Đơn vị
         */
        private String unit;
        
        /**
         * Danh sách các batch được pick theo FIFO
         */
        private List<BatchPick> batchPicks = new ArrayList<>();
        
        /**
         * Số lượng thiếu (nếu không đủ stock)
         */
        private BigDecimal shortageQuantity;
        
        /**
         * Có đủ nguyên liệu không
         */
        private Boolean isSufficient;
    }
    
    /**
     * Chi tiết pick từ một MaterialBatch
     */
    @Data
    public static class BatchPick {
        private Long materialBatchId;
        private String batchNumber;
        private LocalDate manufacturingDate;
        private LocalDate expiryDate;
        
        /**
         * Tồn kho hiện tại trước khi pick
         */
        private BigDecimal currentStock;
        
        /**
         * Số lượng sẽ lấy từ batch này
         */
        private BigDecimal pickQuantity;
        
        /**
         * Số lượng còn lại sau khi pick
         */
        private BigDecimal remainingStock;
        
        private String unit;
        
        /**
         * Đơn giá của batch này
         */
        private BigDecimal unitCost;
        
        /**
         * Thành tiền = pickQuantity * unitCost
         */
        private BigDecimal amount;
    }
    
    @Data
    public static class ConsumptionSummary {
        /**
         * Tổng số loại NVL
         */
        private Integer materialCount;
        
        /**
         * Số loại NVL đủ
         */
        private Integer sufficientCount;
        
        /**
         * Số loại NVL thiếu
         */
        private Integer shortageCount;
        
        /**
         * Tổng giá trị ước tính (VNĐ)
         */
        private BigDecimal totalEstimatedCost;
        
        /**
         * Có thể thực hiện được không (tất cả NVL đều đủ)
         */
        private Boolean canExecute;
    }
}
