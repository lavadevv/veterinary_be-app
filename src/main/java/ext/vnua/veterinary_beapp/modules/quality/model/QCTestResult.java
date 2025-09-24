package ext.vnua.veterinary_beapp.modules.quality.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "qc_test_results", indexes = {
        @Index(name = "idx_qc_test_result_record", columnList = "qc_record_id"),
        @Index(name = "idx_qc_test_result_standard_item", columnList = "standard_item_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QCTestResult extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qc_record_id", nullable = false)
    private ProductionQCRecord qcRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_item_id", nullable = false)
    private QCStandardItem standardItem;

    @Column(name = "numeric_value", precision = 15, scale = 6)
    private BigDecimal numericValue;

    @Column(name = "text_value", length = 500)
    private String textValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @Column(name = "option_value", length = 100)
    private String optionValue;

    @Column(name = "is_within_spec", nullable = false)
    private Boolean isWithinSpec = true;

    @Column(name = "deviation", precision = 15, scale = 6)
    private BigDecimal deviation;

    @Column(name = "deviation_percent", precision = 8, scale = 4)
    private BigDecimal deviationPercent;

    @Column(name = "test_date", nullable = false)
    private LocalDateTime testDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tester_id", nullable = false)
    private User tester;

    @Column(name = "test_method", length = 100)
    private String testMethod;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "retest_count", nullable = false)
    private Integer retestCount = 0;
}
