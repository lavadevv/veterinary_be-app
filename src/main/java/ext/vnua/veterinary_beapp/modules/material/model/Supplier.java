package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Supplier extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_code", unique = true, nullable = false)
    private String supplierCode;

    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "manufacturer_id",
            foreignKey = @ForeignKey(name = "fk_suppliers_manufacturer"))
    private Manufacturer manufacturer;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "gmp_certificate")
    private String gmpCertificate;

    @Column(name = "gmp_expiry_date")
    private java.time.LocalDate gmpExpiryDate;

    @Column(name = "country_of_origin")
    private String countryOfOrigin;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Relationships
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Material> materials;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== THÔNG TIN NHÀ CUNG CẤP ===\n");
        sb.append("Mã NCC              : ").append(supplierCode).append("\n");
        sb.append("Tên NCC             : ").append(supplierName).append("\n");
        if (manufacturer != null) {
            sb.append("NSX           : ").append(manufacturer.getManufacturerName())
                    .append(" (").append(manufacturer.getManufacturerCode()).append(")\n");
        }
        if (address != null) sb.append("Địa chỉ             : ").append(address).append("\n");
        if (registrationNumber != null) sb.append("Số đăng ký          : ").append(registrationNumber).append("\n");
        if (phone != null) sb.append("Số điện thoại       : ").append(phone).append("\n");
        if (email != null) sb.append("Email               : ").append(email).append("\n");
        if (contactPerson != null) sb.append("Người liên hệ       : ").append(contactPerson).append("\n");
        if (gmpCertificate != null) sb.append("Chứng chỉ GMP       : ").append(gmpCertificate).append("\n");
        if (gmpExpiryDate != null) sb.append("Ngày hết hạn GMP    : ").append(gmpExpiryDate).append("\n");
        if (countryOfOrigin != null) sb.append("Quốc gia            : ").append(countryOfOrigin).append("\n");
        sb.append("Trạng thái hoạt động: ").append(isActive ? "Đang hoạt động" : "Ngừng hoạt động").append("\n");
        if (notes != null) sb.append("Ghi chú             : ").append(notes).append("\n");
        return sb.toString();
    }

}