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

    @ManyToOne(fetch = FetchType.EAGER)
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
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<MaterialBatch> materialBatches;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== VỊ TRÍ TRONG KHO ===\n");
        sb.append("Mã vị trí     : ").append(locationCode).append("\n");
        sb.append("Thuộc kho     : ").append(warehouse != null ? warehouse.getWarehouseName() : "Không xác định").append("\n");
        sb.append("Kệ            : ").append(shelf != null ? shelf : "Không rõ").append("\n");
        sb.append("Tầng          : ").append(floor != null ? floor : "Không rõ").append("\n");
        sb.append("Chi tiết vị trí: ").append(positionDetail != null ? positionDetail : "Không có").append("\n");
        sb.append("Sức chứa tối đa: ").append(maxCapacity != null ? maxCapacity + " đơn vị" : "Chưa xác định").append("\n");
        sb.append("Đang chứa     : ").append(currentCapacity != null ? currentCapacity + " đơn vị" : "0").append("\n");
        sb.append("Trạng thái    : ").append(isAvailable ? "Có sẵn để sử dụng" : "Đang đầy/không sử dụng được").append("\n");
        return sb.toString();
    }

}
