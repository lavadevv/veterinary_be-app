package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "manufacturers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Manufacturer extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "manufacturer_code", unique = true, nullable = false, length = 50)
    private String manufacturerCode;

    @Column(name = "manufacturer_name", nullable = false, length = 255)
    private String manufacturerName;

    @Column(name = "country_of_origin", length = 100)
    private String countryOfOrigin;

    @Column(name = "official_distributor_name", length = 255)
    private String officialDistributorName;

    @Column(name = "official_distributor_phone", length = 30)
    private String officialDistributorPhone;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
