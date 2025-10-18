package ext.vnua.veterinary_beapp.modules.pricing.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "pricing_product_line",
        indexes = {
                @Index(name = "idx_ppl_product", columnList = "product_id"),
                @Index(name = "idx_ppl_product_stt", columnList = "product_id, stt")
        }
)
@Getter @Setter
public class ProductPricingLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Gắn với sản phẩm */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** Thứ tự hiển thị */
    @Column(nullable = false)
    private Integer stt;

    /** Mã chi phí user điền (tuỳ chọn) */
    @Column(length = 120)
    private String manualCode;

    /** Mã bảng CPSX để lấy unitCost */
    @Column(length = 200, nullable = false)
    private String sheetCode;

    /** Kích cỡ đóng gói (ml/g) */
    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal packSize;

    /** Tên quy cách hiển thị */
    @Column(length = 255, nullable = false)
    private String specName;

    /** % lợi nhuận dạng thập phân (0.09 = 9%) */
    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal profitPercent;

    /** Cho phép bật/tắt dòng mà không xoá */
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
