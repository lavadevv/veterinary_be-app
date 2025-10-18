package ext.vnua.veterinary_beapp.modules.pcost.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "labor_rates",
        indexes = {
                @Index(name = "idx_labor_code", columnList = "code", unique = true),
                @Index(name = "idx_labor_effective", columnList = "effective_date")
        })
@Data @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LaborRate extends AuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã nhân công (unique) – ví dụ: LAB-SKILL-A-2025 */
    @Column(name = "code", nullable = false, unique = true, length = 150)
    private String code;

    /** Tên hiển thị – ví dụ: Nhân công bậc A (2025) */
    @Column(name = "name", length = 300)
    private String name;

    /** Đơn vị tính – thường là "hour" hoặc "shift" */
    @Column(name = "unit", length = 50)
    private String unit = "hour";

    /** Đơn giá/đơn vị (VD 25000.00) */
    @Column(name = "price_per_unit", precision = 18, scale = 2, nullable = false)
    private BigDecimal pricePerUnit = BigDecimal.ZERO;

    /** Ngày hiệu lực */
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate = LocalDate.now();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
