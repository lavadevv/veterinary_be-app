package ext.vnua.veterinary_beapp.modules.product.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.product.enums.ProductBatchStatus;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrder;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_batches", indexes = {
        @Index(name = "idx_batch_product", columnList = "product_id"),
        @Index(name = "idx_batch_number", columnList = "batch_number", unique = true),
        @Index(name = "idx_batch_status", columnList = "status"),
        @Index(name = "idx_batch_expiry", columnList = "expiry_date"),
        @Index(name = "idx_batch_manufacturing", columnList = "manufacturing_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductBatch extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_id", nullable = false)
    private ProductFormula formula;

    @Column(name = "batch_number", unique = true, nullable = false)
    private String batchNumber; // Amoxcoli500 001 07 25

    @Column(name = "manufacturing_date", nullable = false)
    private LocalDate manufacturingDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "planned_quantity", nullable = false, precision = 12, scale = 6)
    private BigDecimal plannedQuantity;

    @Column(name = "actual_quantity", precision = 12, scale = 6)
    private BigDecimal actualQuantity;

    @Column(name = "yield_percentage", precision = 5, scale = 2)
    private BigDecimal yieldPercentage; // Hiệu suất sản xuất

    @Column(name = "rejected_quantity", precision = 12, scale = 6)
    private BigDecimal rejectedQuantity = BigDecimal.ZERO;

    @Column(name = "current_stock", precision = 12, scale = 6)
    private BigDecimal currentStock;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductBatchStatus status = ProductBatchStatus.IN_PROGRESS;
//
//    @Column(name = "warehouse_location")
//    private String warehouseLocation; // Mã kho, kệ, vị trí thực tế

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qc_approved_by")
    private User qcApprovedBy;

    @Column(name = "qc_approved_at")
    private LocalDateTime qcApprovedAt;

//    @Column(name = "production_order_id")
//    private Long productionOrderId; // Link với production order

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id")
    private ProductionOrder productionOrder;

    @Column(name = "qc_certificate_path")
    private String qcCertificatePath;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}

