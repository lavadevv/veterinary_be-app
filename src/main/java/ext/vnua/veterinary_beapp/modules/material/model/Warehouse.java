package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "warehouses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Warehouse extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_code", unique = true, nullable = false)
    private String warehouseCode;

    @Column(name = "warehouse_name", nullable = false)
    private String warehouseName;

    @Column(name = "warehouse_type")
    private String warehouseType; // Kho nguyên liệu, kho bao bì, kho thành phẩm...

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "manager_name")
    private String managerName;

    @Column(name = "temperature_range")
    private String temperatureRange;

    @Column(name = "humidity_range")
    private String humidityRange;

    @Column(name = "special_conditions", columnDefinition = "TEXT")
    private String specialConditions;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Relationships
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Location> locations;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== THÔNG TIN KHO ===\n");
        sb.append("Mã kho        : ").append(warehouseCode).append("\n");
        sb.append("Tên kho       : ").append(warehouseName).append("\n");
        sb.append("Loại kho      : ").append(warehouseType != null ? warehouseType : "Không xác định").append("\n");
        sb.append("Người quản lý : ").append(managerName != null ? managerName : "Chưa có").append("\n");
        sb.append("Địa chỉ       : ").append(address != null ? address : "Chưa cập nhật").append("\n");
        sb.append("Nhiệt độ      : ").append(temperatureRange != null ? temperatureRange : "Không rõ").append("\n");
        sb.append("Độ ẩm         : ").append(humidityRange != null ? humidityRange : "Không rõ").append("\n");
        sb.append("Điều kiện đặc biệt: ").append(specialConditions != null ? specialConditions : "Không có").append("\n");
        sb.append("Trạng thái    : ").append(isActive ? "Đang hoạt động" : "Ngừng hoạt động").append("\n");
        return sb.toString();
    }

}
