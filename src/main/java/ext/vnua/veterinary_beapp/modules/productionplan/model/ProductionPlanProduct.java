package ext.vnua.veterinary_beapp.modules.productionplan.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.pcost.model.ProductionCostSheet;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "production_plan_products",
        indexes = {
                @Index(name = "idx_plan_product_plan", columnList = "plan_id"),
                @Index(name = "idx_plan_product_product", columnList = "product_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductionPlanProduct extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_plan_product_plan"))
    private ProductionPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_plan_product_product"))
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_cost_sheet_id",
            foreignKey = @ForeignKey(name = "fk_plan_product_cost_sheet"))
    private ProductionCostSheet productionCostSheet;

    @Column(name = "planned_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal plannedQuantity;

    @Column(name = "actual_quantity", precision = 15, scale = 3)
    private BigDecimal actualQuantity;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Column(name = "planned_unit_cost", precision = 18, scale = 2)
    private BigDecimal plannedUnitCost;

    @Column(name = "planned_total_cost", precision = 18, scale = 2)
    private BigDecimal plannedTotalCost;

    @Column(name = "production_cost_sheet_code", length = 150)
    private String productionCostSheetCode;

    @Column(name = "production_cost_sheet_name", length = 300)
    private String productionCostSheetName;

    @Column(name = "production_cost_spec_units")
    private Integer productionCostSpecUnits;

    @Column(name = "product_brand", length = 200)
    private String productBrand;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
