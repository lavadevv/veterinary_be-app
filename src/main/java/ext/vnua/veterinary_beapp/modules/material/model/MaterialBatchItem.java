package ext.vnua.veterinary_beapp.modules.material.model;

import ext.vnua.veterinary_beapp.modules.audits.entity.AuditableEntity;
import ext.vnua.veterinary_beapp.modules.material.enums.TestStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Đại diện cho MỘT vật liệu trong một lô nhập hàng
 * Một MaterialBatch (lô nhập) có thể chứa nhiều MaterialBatchItem (nhiều vật liệu khác nhau)
 */
@Entity
@Table(name = "material_batch_items", indexes = {
        @Index(name = "idx_batch_item_material", columnList = "material_id"),
        @Index(name = "idx_batch_item_batch", columnList = "batch_id"),
        @Index(name = "idx_batch_item_internal_code", columnList = "internal_item_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MaterialBatchItem extends AuditableEntity {

    private static final int QTY_SCALE = 3;
    private static final int MONEY_SCALE = 2;
    private static final int PERCENT_SCALE = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Lô nhập hàng cha */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_batch_items_batch"))
    private MaterialBatch batch;

    /** Vật liệu cụ thể trong lô này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_batch_items_material"))
    private Material material;

    /** Nhà cung cấp của vật liệu này (có thể khác batch container) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id",
            foreignKey = @ForeignKey(name = "fk_batch_items_supplier"))
    private Supplier supplier;

    /** Nhà sản xuất của vật liệu này (có thể khác batch container) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id",
            foreignKey = @ForeignKey(name = "fk_batch_items_manufacturer"))
    private Manufacturer manufacturer;

    /** Mã item nội bộ (unique cho mỗi material trong mỗi lô) */
    @Column(name = "internal_item_code", unique = true, length = 100)
    private String internalItemCode;

    /** Mã lô của nhà sản xuất cho vật liệu này (có thể khác nhau giữa các material) */
    @Column(name = "manufacturer_batch_number", length = 100)
    private String manufacturerBatchNumber;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "received_quantity", nullable = false, precision = 15, scale = QTY_SCALE)
    private BigDecimal receivedQuantity;

    @Column(name = "current_quantity", nullable = false, precision = 15, scale = QTY_SCALE)
    private BigDecimal currentQuantity;

    /** Đơn giá TRƯỚC THUẾ (per unit) */
    @Column(name = "unit_price", precision = 18, scale = MONEY_SCALE)
    private BigDecimal unitPrice;

    /** % Thuế */
    @Column(name = "tax_percent", precision = 9, scale = PERCENT_SCALE)
    private BigDecimal taxPercent = BigDecimal.ZERO;

    /** Thành tiền trước thuế = unitPrice * receivedQuantity */
    @Column(name = "subtotal_amount", precision = 18, scale = MONEY_SCALE)
    private BigDecimal subtotalAmount;

    /** Tiền thuế */
    @Column(name = "tax_amount", precision = 18, scale = MONEY_SCALE)
    private BigDecimal taxAmount;

    /** Tổng tiền */
    @Column(name = "total_amount", precision = 18, scale = MONEY_SCALE)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_status", nullable = false)
    private TestStatus testStatus = TestStatus.CHO_KIEM_NGHIEM;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_status", nullable = false)
    private UsageStatus usageStatus = UsageStatus.CACH_LY;

    /** Vị trí kho cụ thể cho item này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id",
            foreignKey = @ForeignKey(name = "fk_batch_items_location"))
    private Location location;

    /** Vị trí kệ cụ thể */
    @Column(name = "shelf_location", length = 100)
    private String shelfLocation;

    /** Đường dẫn file ảnh của item này */
    @Column(name = "image_path", length = 500)
    private String imagePath;

    /** Danh sách hàm lượng hoạt chất của item này */
    @OneToMany(mappedBy = "batchItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MaterialBatchItemActiveIngredient> batchItemActiveIngredients = new ArrayList<>();

    @Column(name = "coa_number", length = 100)
    private String coaNumber;

    @Column(name = "test_report_number", length = 100)
    private String testReportNumber;

    @Column(name = "test_results", columnDefinition = "JSON")
    private String testResults;

    @Column(name = "quarantine_reason", columnDefinition = "TEXT")
    private String quarantineReason;

    @Column(name = "coa_file_path", length = 500)
    private String coaFilePath;

    @Column(name = "msds_file_path", length = 500)
    private String msdsFilePath;

    @Column(name = "test_certificate_path", length = 500)
    private String testCertificatePath;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "reserved_quantity", precision = 15, scale = QTY_SCALE)
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(name = "available_quantity", precision = 15, scale = QTY_SCALE)
    private BigDecimal availableQuantity;

    // ======================== Lifecycle =========================

    @PrePersist
    public void prePersist() {
        normalizeNulls();
        recalcAmounts();
        recalcAvailability();
    }

    @PreUpdate
    public void preUpdate() {
        normalizeNulls();
        recalcAmounts();
        recalcAvailability();
    }

    @PostLoad
    public void postLoad() {
        // Recalculate amounts after loading from DB
        normalizeNulls();
        recalcAmounts();
        recalcAvailability();
    }

    private void normalizeNulls() {
        if (receivedQuantity == null) receivedQuantity = BigDecimal.ZERO.setScale(QTY_SCALE, RoundingMode.HALF_UP);
        if (currentQuantity == null) currentQuantity = BigDecimal.ZERO.setScale(QTY_SCALE, RoundingMode.HALF_UP);
        if (reservedQuantity == null) reservedQuantity = BigDecimal.ZERO.setScale(QTY_SCALE, RoundingMode.HALF_UP);
        if (unitPrice == null) unitPrice = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        if (taxPercent == null) taxPercent = BigDecimal.ZERO.setScale(PERCENT_SCALE, RoundingMode.HALF_UP);
    }

    private void recalcAvailability() {
        availableQuantity = currentQuantity.subtract(reservedQuantity);
        if (availableQuantity.signum() < 0) {
            availableQuantity = BigDecimal.ZERO.setScale(QTY_SCALE, RoundingMode.HALF_UP);
        } else {
            availableQuantity = availableQuantity.setScale(QTY_SCALE, RoundingMode.HALF_UP);
        }
    }

    private void recalcAmounts() {
        BigDecimal subtotal = unitPrice.multiply(receivedQuantity)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        BigDecimal tax = subtotal
                .multiply(taxPercent)
                .divide(new BigDecimal("100"), MONEY_SCALE, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.add(tax)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        this.subtotalAmount = subtotal;
        this.taxAmount = tax;
        this.totalAmount = total;
    }

    // ======================== Analysis Methods =========================

    public Boolean isAllActiveIngredientsQualified() {
        if (batchItemActiveIngredients == null || batchItemActiveIngredients.isEmpty()) {
            return null;
        }

        boolean hasData = false;
        for (MaterialBatchItemActiveIngredient ingredient : batchItemActiveIngredients) {
            Boolean qualified = ingredient.isQualified();
            if (qualified != null) {
                hasData = true;
                if (!qualified) {
                    return false;
                }
            }
        }

        return hasData ? true : null;
    }

    public String getQualificationStatus() {
        Boolean qualified = isAllActiveIngredientsQualified();
        if (qualified == null) {
            return "Chưa có dữ liệu";
        }
        return qualified ? "Đạt" : "Không đạt";
    }

    public List<String> getUnqualifiedIngredients() {
        List<String> unqualified = new ArrayList<>();
        if (batchItemActiveIngredients != null) {
            for (MaterialBatchItemActiveIngredient ingredient : batchItemActiveIngredients) {
                if (Boolean.FALSE.equals(ingredient.isQualified())
                        && ingredient.getActiveIngredient() != null) {
                    unqualified.add(ingredient.getActiveIngredient().getIngredientName());
                }
            }
        }
        return unqualified;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== CHI TIẾT VẬT LIỆU TRONG LÔ ===\n");
        sb.append("Mã item nội bộ      : ").append(internalItemCode != null ? internalItemCode : "Chưa có").append("\n");
        if (material != null) {
            sb.append("Vật liệu            : ").append(material.getMaterialName()).append(" (").append(material.getMaterialCode()).append(")\n");
        }
        if (manufacturerBatchNumber != null) sb.append("Mã lô NSX           : ").append(manufacturerBatchNumber).append("\n");
        if (manufacturingDate != null) sb.append("Ngày sản xuất       : ").append(manufacturingDate).append("\n");
        if (expiryDate != null) sb.append("Hạn sử dụng         : ").append(expiryDate).append("\n");
        sb.append("Số lượng nhập       : ").append(receivedQuantity).append("\n");
        sb.append("Số lượng hiện tại   : ").append(currentQuantity).append("\n");
        if (unitPrice != null) sb.append("Đơn giá (trước thuế): ").append(unitPrice).append(" VND\n");
        if (taxPercent != null) sb.append("Thuế (%)            : ").append(taxPercent.stripTrailingZeros().toPlainString()).append("\n");
        if (subtotalAmount != null) sb.append("Thành tiền (chưa thuế): ").append(subtotalAmount).append(" VND\n");
        if (taxAmount != null) sb.append("Tiền thuế           : ").append(taxAmount).append(" VND\n");
        if (totalAmount != null) sb.append("Tổng tiền           : ").append(totalAmount).append(" VND\n");
        sb.append("Tình trạng kiểm nghiệm: ").append(testStatus != null ? testStatus.getDisplayName() : "").append("\n");
        sb.append("Trạng thái sử dụng  : ").append(usageStatus != null ? usageStatus.getDisplayName() : "").append("\n");
        if (location != null) sb.append("Vị trí kho          : ").append(location.getLocationCode()).append("\n");
        if (shelfLocation != null) sb.append("Vị trí kệ           : ").append(shelfLocation).append("\n");
        sb.append("Đã giữ chỗ          : ").append(reservedQuantity).append("\n");
        sb.append("Khả dụng            : ").append(availableQuantity).append("\n");
        return sb.toString();
    }
}
