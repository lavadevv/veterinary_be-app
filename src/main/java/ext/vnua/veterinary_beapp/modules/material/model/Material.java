package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.material.enums.MaterialForm;
import ext.vnua.veterinary_beapp.modules.material.enums.MaterialType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Material extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "material_code", unique = true, nullable = false)
    private String materialCode;

    @Column(name = "material_name", nullable = false)
    private String materialName;

    @Column(name = "short_name")
    private String shortName;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false)
    private MaterialType materialType;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_form")
    private MaterialForm materialForm;

    @Column(name = "active_ingredient", columnDefinition = "TEXT")
    private String activeIngredient;

    @Column(name = "purity_percentage")
    private Double purityPercentage;

    @Column(name = "iu_per_gram")
    private Double iuPerGram;

    @Column(name = "color")
    private String color;

    @Column(name = "odor")
    private String odor;

    @Column(name = "moisture_content")
    private Double moistureContent;

    @Column(name = "viscosity")
    private Double viscosity;

    @Column(name = "unit_of_measure", nullable = false)
    private String unitOfMeasure; // kg, g, ml, L, cái, cuộn, lọ, m2...

    @Column(name = "standard_applied", columnDefinition = "TEXT")
    private String standardApplied; // USP, BP, EP, tiêu chuẩn nhà sản xuất...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "minimum_stock_level")
    private Double minimumStockLevel;

    @Column(name = "current_stock")
    private Double currentStock = 0.0;

    @Column(name = "fixed_price")
    private Double fixedPrice; // Đơn giá cố định để tính giá thành

    @Column(name = "requires_cold_storage", nullable = false)
    private Boolean requiresColdStorage = false;

    @Column(name = "special_handling", columnDefinition = "TEXT")
    private String specialHandling;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Relationships
    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaterialBatch> batches;
}

