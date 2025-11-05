package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.material.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "material_movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MaterialMovement extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30)
    private MovementType movementType;

    /** Batch nguồn (có thể null khi RECEIVE) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_batch_id",
            foreignKey = @ForeignKey(name = "fk_mov_source_batch"))
    private MaterialBatch sourceBatch;

    /** Batch đích (có thể null khi CONSUME) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_batch_id",
            foreignKey = @ForeignKey(name = "fk_mov_target_batch"))
    private MaterialBatch targetBatch;

    /** Vị trí nguồn/đích để truy vết nhanh */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_location_id",
            foreignKey = @ForeignKey(name = "fk_mov_source_loc"))
    private Location sourceLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_location_id",
            foreignKey = @ForeignKey(name = "fk_mov_target_loc"))
    private Location targetLocation;

    @Column(name = "quantity", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "movement_time", nullable = false)
    private LocalDateTime movementTime = LocalDateTime.now();

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
