package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.material.enums.BatchStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.TestStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import jakarta.persistence.*;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NamedEntityGraph(
        name = MaterialBatch.ENTITY_GRAPH_WITH_DETAILS,
        attributeNodes = {
                @NamedAttributeNode(value = "batchItems", subgraph = "batchItemDetails"),
                @NamedAttributeNode(value = "location", subgraph = "locationDetails"),
                @NamedAttributeNode("supplier"),
                @NamedAttributeNode("manufacturer")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "batchItemDetails",
                        attributeNodes = {
                                @NamedAttributeNode(value = "material", subgraph = "materialDetails"),
                                @NamedAttributeNode("location"),
                                @NamedAttributeNode(value = "batchItemActiveIngredients", subgraph = "activeIngredientDetails")
                        }
                ),
                @NamedSubgraph(
                        name = "materialDetails",
                        attributeNodes = {
                                @NamedAttributeNode("supplier"),
                                @NamedAttributeNode("unitOfMeasure"),
                                @NamedAttributeNode("materialCategory"),
                                @NamedAttributeNode("materialFormType")
                        }
                ),
                @NamedSubgraph(
                        name = "locationDetails",
                        attributeNodes = {
                                @NamedAttributeNode("warehouse")
                        }
                ),
                @NamedSubgraph(
                        name = "activeIngredientDetails",
                        attributeNodes = {
                                @NamedAttributeNode("activeIngredient")
                        }
                )
        }
)
@Entity
@Table(name = "material_batches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MaterialBatch extends AuditableEntity {

    public static final String ENTITY_GRAPH_WITH_DETAILS = "MaterialBatch.withDetails";

    private static final int QTY_SCALE = 3;
    private static final int MONEY_SCALE = 2;
    private static final int PERCENT_SCALE = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã lô nhập hàng (một lần nhập có thể chứa nhiều vật liệu) */
    @Column(name = "batch_number", nullable = false, unique = true, length = 100)
    private String batchNumber;

    /** Mã lô nội bộ */
    @Column(name = "internal_batch_code", unique = true, length = 100)
    private String internalBatchCode;

    /** Ngày nhập hàng */
    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    /** Vị trí kho mặc định cho lô (có thể override ở từng item) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id",
            foreignKey = @ForeignKey(name = "fk_batches_location"))
    private Location location;

    // ====== THÔNG TIN NCC/NSX CHUNG CỦA LÔ NHẬP ======

    /** Nhà cung cấp của lô nhập này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id",
            foreignKey = @ForeignKey(name = "fk_batches_supplier"))
    private Supplier supplier;

    /** Nhà sản xuất chung (nếu có) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id",
            foreignKey = @ForeignKey(name = "fk_batches_manufacturer"))
    private Manufacturer manufacturer;

    /** Quốc gia xuất xứ chung */
    @Column(name = "country_of_origin", length = 100)
    private String countryOfOrigin;

    /** Số hóa đơn nhập hàng */
    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    /** Tổng tiền của toàn bộ lô (tính từ các items) */
    @Column(name = "total_amount", precision = 18, scale = MONEY_SCALE)
    private BigDecimal totalAmount;

    // ====== DANH SÁCH CÁC VẬT LIỆU TRONG LÔ ======

    /** Các vật liệu cụ thể trong lô nhập này */
    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MaterialBatchItem> batchItems = new ArrayList<>();

    // ====== THÔNG TIN CHUNG ======

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** Trạng thái chung của lô */
    @Enumerated(EnumType.STRING)
    @Column(name = "batch_status", length = 50)
    private BatchStatus batchStatus = BatchStatus.ACTIVE; // ACTIVE, COMPLETED, CANCELLED

    // ======================== Lifecycle =========================

    @PrePersist
    public void prePersist() {
        recalcTotalAmount();
    }

    @PreUpdate
    public void preUpdate() {
        recalcTotalAmount();
    }

    @PostLoad
    public void postLoad() {
        // Recalculate totalAmount after loading from DB
        // to ensure it's up-to-date with current items
        recalcTotalAmount();
    }

    /**
     * Tính tổng tiền của toàn bộ lô từ các items
     */
    private void recalcTotalAmount() {
        if (batchItems == null || batchItems.isEmpty()) {
            this.totalAmount = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            return;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (MaterialBatchItem item : batchItems) {
            if (item.getTotalAmount() != null) {
                sum = sum.add(item.getTotalAmount());
            }
        }
        this.totalAmount = sum.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    // ======================== Business Methods =========================

    /**
     * Thêm một vật liệu vào lô
     */
    public void addBatchItem(MaterialBatchItem item) {
        if (batchItems == null) {
            batchItems = new ArrayList<>();
        }
        batchItems.add(item);
        item.setBatch(this);
    }

    /**
     * Xóa một vật liệu khỏi lô
     */
    public void removeBatchItem(MaterialBatchItem item) {
        if (batchItems != null) {
            batchItems.remove(item);
            item.setBatch(null);
        }
    }

    /**
     * Lấy tổng số lượng vật liệu trong lô
     */
    public int getTotalItemsCount() {
        return batchItems != null ? batchItems.size() : 0;
    }

    /**
     * Kiểm tra tất cả items trong lô có đạt chuẩn hay không
     */
    public Boolean isAllItemsQualified() {
        if (batchItems == null || batchItems.isEmpty()) {
            return null;
        }

        boolean hasData = false;
        for (MaterialBatchItem item : batchItems) {
            Boolean qualified = item.isAllActiveIngredientsQualified();
            if (qualified != null) {
                hasData = true;
                if (!qualified) {
                    return false;
                }
            }
        }

        return hasData ? true : null;
    }

    /**
     * Lấy danh sách các items không đạt chuẩn
     */
    public List<MaterialBatchItem> getUnqualifiedItems() {
        List<MaterialBatchItem> unqualified = new ArrayList<>();
        if (batchItems != null) {
            for (MaterialBatchItem item : batchItems) {
                if (Boolean.FALSE.equals(item.isAllActiveIngredientsQualified())) {
                    unqualified.add(item);
                }
            }
        }
        return unqualified;
    }

    // ========================= toString =========================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== THÔNG TIN LÔ NHẬP HÀNG ===\n");
        sb.append("Mã lô nhập           : ").append(batchNumber).append("\n");
        sb.append("Mã lô nội bộ         : ").append(internalBatchCode != null ? internalBatchCode : "Chưa có").append("\n");
        sb.append("Ngày nhập            : ").append(receivedDate).append("\n");
        
        if (supplier != null) sb.append("Nhà cung cấp         : ").append(supplier.getSupplierName()).append("\n");
        if (manufacturer != null) sb.append("Nhà sản xuất         : ").append(manufacturer.getManufacturerName()).append("\n");
        if (countryOfOrigin != null) sb.append("Xuất xứ              : ").append(countryOfOrigin).append("\n");
        if (invoiceNumber != null) sb.append("Số hóa đơn           : ").append(invoiceNumber).append("\n");
        if (location != null) sb.append("Vị trí kho mặc định  : ").append(location.getLocationCode()).append("\n");
        
        sb.append("Tổng tiền lô         : ").append(totalAmount != null ? totalAmount : BigDecimal.ZERO).append(" VND\n");
        sb.append("Số lượng vật liệu    : ").append(getTotalItemsCount()).append("\n");
        sb.append("Trạng thái lô        : ").append(batchStatus).append("\n");
        
        if (notes != null) sb.append("Ghi chú              : ").append(notes).append("\n");
        
        // Hiển thị thông tin các items
        if (batchItems != null && !batchItems.isEmpty()) {
            sb.append("\n--- Chi tiết vật liệu trong lô ---\n");
            for (int i = 0; i < batchItems.size(); i++) {
                MaterialBatchItem item = batchItems.get(i);
                sb.append("[").append(i + 1).append("] ");
                if (item.getMaterial() != null) {
                    sb.append(item.getMaterial().getMaterialName());
                    sb.append(" (").append(item.getMaterial().getMaterialCode()).append(")");
                }
                sb.append(" - SL: ").append(item.getCurrentQuantity());
                if (item.getMaterial() != null && item.getMaterial().getUnitOfMeasure() != null) {
                    sb.append(" ").append(item.getMaterial().getUnitOfMeasure().getName());
                }
                sb.append(" - Giá: ").append(item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO).append(" VND");
                sb.append("\n");
            }
        }
        
        return sb.toString();
    }
}
