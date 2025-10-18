package ext.vnua.veterinary_beapp.modules.pcost.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_cost_sheets",
        indexes = {
                @Index(name="idx_pcs_product", columnList="product_id"),
                @Index(name="idx_pcs_code", columnList="sheet_code", unique=true),
                @Index(name="idx_pcs_effective", columnList="effective_date")
        })
@Data @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductionCostSheet extends AuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId; // optional

    @Column(name = "sheet_code", nullable = false, unique = true, length = 150)
    private String sheetCode;

    @Column(name = "sheet_name", length = 300)
    private String sheetName;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate = LocalDate.now();

    /** Số SP tạo thành từ 1 set chi phí */
    @Column(name = "spec_units", nullable = false)
    private Integer specUnits = 1;

    /** Các tổng này sẽ được tính ở Service khi trả DTO (động theo master) */
    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "unit_cost", precision = 18, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "sheet", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNo ASC, id ASC")
    private List<ProductionCostItem> items = new ArrayList<>();

    @PrePersist @PreUpdate
    public void ensureNonNullTotals() {
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        if (unitCost == null)    unitCost = BigDecimal.ZERO;
        if (effectiveDate == null) effectiveDate = LocalDate.now();
        if (specUnits == null || specUnits <= 0) specUnits = 1;
    }
}
