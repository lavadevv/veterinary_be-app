package ext.vnua.veterinary_beapp.modules.productionplan.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.productionplan.enums.ProductionPlanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_lots",
        indexes = {
                @Index(name = "idx_lot_month_year", columnList = "plan_year, plan_month"),
                @Index(name = "idx_lot_status", columnList = "status"),
                @Index(name = "idx_lot_number", columnList = "lot_number", unique = true)
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductionLot extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Lot number formatted as xxmmyy (e.g. 011125). */
    @Column(name = "lot_number", nullable = false, unique = true, length = 6)
    private String lotNumber;

    @Column(name = "sequence_in_month", nullable = false)
    private Integer sequenceInMonth;

    @Column(name = "plan_month", nullable = false)
    private Integer planMonth;

    @Column(name = "plan_year", nullable = false)
    private Integer planYear;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductionPlanStatus status = ProductionPlanStatus.PLANNING;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "lot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionPlan> plans = new ArrayList<>();

    public void addPlan(ProductionPlan plan) {
        plan.setLot(this);
        this.plans.add(plan);
    }
}

