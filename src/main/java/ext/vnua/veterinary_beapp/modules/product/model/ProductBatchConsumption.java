package ext.vnua.veterinary_beapp.modules.product.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatchItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "product_batch_consumptions", indexes = {
        @Index(name = "idx_pbc_batch", columnList = "product_batch_id"),
        @Index(name = "idx_pbc_material_batch_item", columnList = "material_batch_item_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductBatchConsumption extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Batch thành phẩm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_batch_id", nullable = false)
    private ProductBatch productBatch;

    // Item NVL đã dùng (changed from MaterialBatch to MaterialBatchItem)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_batch_item_id", nullable = false)
    private MaterialBatchItem materialBatchItem;

    // Số lượng planned (reserve khi issue)
    @Column(name = "planned_quantity", precision = 12, scale = 6, nullable = false)
    private BigDecimal plannedQuantity;

    // Số lượng thực tế dùng (update khi complete)
    @Column(name = "actual_quantity", precision = 12, scale = 6)
    private BigDecimal actualQuantity = BigDecimal.ZERO;
}