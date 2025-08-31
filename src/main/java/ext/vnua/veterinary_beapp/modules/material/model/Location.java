package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Location extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "location_code", nullable = false)
    private String locationCode; // Kệ A1, B2, C3...

    @Column(name = "shelf")
    private String shelf;

    @Column(name = "floor")
    private String floor;

    @Column(name = "position_detail")
    private String positionDetail;

    @Column(name = "max_capacity")
    private Double maxCapacity;

    @Column(name = "current_capacity")
    private Double currentCapacity = 0.0;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    // Relationships
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaterialBatch> materialBatches;
}
