package ext.vnua.veterinary_beapp.modules.pcost.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.material.model.UnitOfMeasure;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@NamedEntityGraph(
        name = "EnergyTariff.withUnitOfMeasure",
        attributeNodes = @NamedAttributeNode("unitOfMeasure")
)
@Entity
@Table(name = "energy_tariffs",
        indexes = {
                @Index(name="idx_et_code", columnList="code", unique = true),
                @Index(name="idx_et_effective", columnList="effective_date")
        })
@Data
@EqualsAndHashCode(callSuper = true)
public class EnergyTariff extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="code", nullable=false, unique = true, length=100)
    private String code;

    @Column(name="name", nullable=false, length=200)
    private String name;

    /** Đơn vị tính – liên kết với UnitOfMeasure module (thường là kWh) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_of_measure_id", foreignKey = @ForeignKey(name = "fk_energy_tariff_uom"))
    private UnitOfMeasure unitOfMeasure;

    /** Đơn giá / đơn vị, ví dụ VND/kWh */
    @Column(name="price_per_unit", precision = 18, scale = 2, nullable=false)
    private BigDecimal pricePerUnit;

    @Column(name="effective_date", nullable=false)
    private LocalDate effectiveDate = LocalDate.now();

    @Column(name="is_active", nullable=false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name="notes", columnDefinition = "TEXT")
    private String notes;
}
