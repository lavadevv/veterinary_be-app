package ext.vnua.veterinary_beapp.modules.production.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import ext.vnua.veterinary_beapp.modules.product.model.ProductBatch;
import ext.vnua.veterinary_beapp.modules.production.enums.ProductionOrderStatus;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "production_orders", indexes = {
        @Index(name = "idx_prod_order_code", columnList = "order_code", unique = true),
        @Index(name = "idx_prod_order_status", columnList = "status"),
        @Index(name = "idx_prod_order_product", columnList = "product_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductionOrder extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true)
    private String orderCode; // Mã lệnh sản xuất (PO-20250913-001)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "planned_quantity", nullable = false, precision = 12, scale = 6)
    private BigDecimal plannedQuantity; // sản lượng kế hoạch

    @Column(name = "actual_quantity", precision = 12, scale = 6)
    private BigDecimal actualQuantity; // sản lượng thực tế (sau khi hoàn tất)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductionOrderStatus status = ProductionOrderStatus.PLANNED;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "created_by")
//    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user",  referencedColumnName = "id")
    private User createdByUser;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedByUser;

    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductBatch> batches;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_line_id")
    private ProductionLine productionLine;

    @Column(name = "yield_rate", precision = 5, scale = 2)
    private BigDecimal yieldRate; // hiệu suất %

    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductionOrderMaterial> materials;

    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductionOrderIssue> issues;

    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductionBatchRecord> records;

}
