package ext.vnua.veterinary_beapp.modules.quality.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.quality.enums.ParameterType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "qc_standard_items", indexes = {
        @Index(name = "idx_qc_standard_item_standard", columnList = "qc_standard_id"),
        @Index(name = "idx_qc_standard_item_param", columnList = "parameter_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QCStandardItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qc_standard_id", nullable = false)
    private QCStandard qcStandard;

    @Column(name = "parameter_name", nullable = false, length = 100)
    private String parameterName;

    @Enumerated(EnumType.STRING)
    @Column(name = "parameter_type", nullable = false, length = 30)
    private ParameterType parameterType;

    @Column(length = 30)
    private String unit;

    @Column(name = "min_value", precision = 15, scale = 6)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 15, scale = 6)
    private BigDecimal maxValue;

    @Column(name = "target_value", precision = 15, scale = 6)
    private BigDecimal targetValue;

    @Column(name = "tolerance", precision = 15, scale = 6)
    private BigDecimal tolerance;

    @Column(name = "option_values", columnDefinition = "TEXT")
    private String optionValues;

    @Column(name = "is_critical", nullable = false)
    private Boolean isCritical = false;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "test_method", length = 100)
    private String testMethod;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
