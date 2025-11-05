package ext.vnua.veterinary_beapp.modules.productionplan.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.productionplan.enums.ProductionPlanStatus;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormula;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_plans",
        indexes = {
                @Index(name = "idx_plan_status", columnList = "status"),
                @Index(name = "idx_plan_formula", columnList = "formula_id"),
                @Index(name = "idx_plan_lot", columnList = "lot_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductionPlan extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", foreignKey = @ForeignKey(name = "fk_plan_lot"))
    private ProductionLot lot;

    @Column(name = "batch_size", nullable = false, precision = 15, scale = 3)
    private BigDecimal batchSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductionPlanStatus status = ProductionPlanStatus.PLANNING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_plan_formula"))
    private ProductFormula formula;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Fetch(FetchMode.SUBSELECT)
    private List<ProductionPlanProduct> productLines = new ArrayList<>();

    public void addProductLine(ProductionPlanProduct product) {
        product.setPlan(this);
        this.productLines.add(product);
    }
}
