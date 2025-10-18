package ext.vnua.veterinary_beapp.modules.pcost.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.pcost.enums.CostItemType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "production_cost_items",
        indexes = {
                @Index(name="idx_pci_sheet", columnList="sheet_id"),
                @Index(name="idx_pci_item_type", columnList="item_type"),
                @Index(name="idx_pci_material", columnList="material_id"),
                @Index(name="idx_pci_labor", columnList="labor_rate_id"),
                @Index(name="idx_pci_energy", columnList="energy_tariff_id")
        })
@Data @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductionCostItem extends AuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Bảng cha */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sheet_id", nullable = false)
    private ProductionCostSheet sheet;

    /** STT hiển thị */
    @Column(name = "order_no")
    private Integer orderNo;

    /** Loại chi phí */
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", length = 20, nullable = false)
    private CostItemType itemType;

    /** Khoá ngoại “mềm” theo từng loại (tuỳ itemType mà 1 trong 3 trường có giá trị) */
    @Column(name = "material_id")
    private Long materialId;

    @Column(name = "labor_rate_id")
    private Long laborRateId;

    @Column(name = "energy_tariff_id")
    private Long energyTariffId;

    /** Đơn vị hiển thị sẽ lấy từ master khi trả DTO -> không lưu cứng ở đây */
    @Column(name="unit_of_measure", length=50)
    private String unitOfMeasure; // optional: không dùng để tính toán

    /** Số lượng do FE nhập (bắt buộc) */
    @Column(name="quantity", precision = 18, scale = 3, nullable = false)
    private BigDecimal quantity = BigDecimal.ONE;

    /** Các cột bên dưới vẫn giữ để tương thích DB, nhưng KHÔNG dùng làm nguồn sự thật */
    @Column(name="unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice;   // không dùng

    @Column(name="amount", precision = 18, scale = 2)
    private BigDecimal amount;      // không dùng

    // Không @PrePersist/@PreUpdate tính amount nữa – tính trong Service theo master
}
