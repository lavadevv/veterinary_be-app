package ext.vnua.veterinary_beapp.modules.product.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.product.enums.RegistrationStatus;
import ext.vnua.veterinary_beapp.modules.product.enums.RegulatoryAuthority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "product_registrations", indexes = {
        @Index(name = "idx_registration_product", columnList = "product_id"),
        @Index(name = "idx_registration_expiry", columnList = "expiry_date"),
        @Index(name = "idx_registration_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductRegistration extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "registration_number", unique = true, nullable = false)
    private String registrationNumber;

    @Column(name = "circulation_permit_number")
    private String circulationPermitNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "regulatory_authority", nullable = false)
    private RegulatoryAuthority regulatoryAuthority; // CUC_THU_Y, CUC_ATTP, BO_NN

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RegistrationStatus status = RegistrationStatus.ACTIVE;

    @Column(name = "registrant_company", nullable = false)
    private String registrantCompany; // Nhà công bố

    @Column(name = "manufacturer_company", nullable = false)
    private String manufacturerCompany; // Nhà sản xuất

    @Column(name = "registration_file_path")
    private String registrationFilePath; // Lưu trữ hồ sơ đăng ký

    @Column(name = "renewal_reminder_days")
    private Integer renewalReminderDays = 90; // Cảnh báo trước bao nhiêu ngày

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
