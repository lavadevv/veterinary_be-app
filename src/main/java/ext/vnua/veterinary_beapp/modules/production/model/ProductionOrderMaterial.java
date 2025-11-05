package ext.vnua.veterinary_beapp.modules.production.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatchItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "production_order_materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
// Chi tiết nguyên liệu cho từng lệnh (theo định mức).
public class ProductionOrderMaterial extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    /**
     * Reference đến MaterialBatchItem (một vật liệu cụ thể trong một lô)
     * Sau khi refactor: MaterialBatch chứa nhiều MaterialBatchItem,
     * nên cần reference trực tiếp đến item chứ không phải batch
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_batch_item_id")
    private MaterialBatchItem materialBatchItem;

    @Column(name = "required_quantity", nullable = false, precision = 12, scale = 6)
    private BigDecimal requiredQuantity; // số lượng theo định mức

    @Column(name = "issued_quantity", precision = 12, scale = 6)
    private BigDecimal issuedQuantity; // số lượng đã cấp phát

    @Column(name = "actual_quantity", precision = 12, scale = 6)
    private BigDecimal actualQuantity; // số lượng thực tế sử dụng

    @Column(name = "status", length = 50)
    private String status; // PENDING / ISSUED / USED / RETURNED

    @Column(name = "notes")
    private String notes;
}
