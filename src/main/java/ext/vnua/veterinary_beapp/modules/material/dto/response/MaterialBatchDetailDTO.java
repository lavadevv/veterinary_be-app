package ext.vnua.veterinary_beapp.modules.material.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO đầy đủ cho hiển thị thông tin lô nguyên liệu
 * Bao gồm tất cả thông tin cần thiết để hiển thị trên UI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialBatchDetailDTO {

    private Long id;

    // ========== THÔNG TIN NGUYÊN LIỆU ==========

    /** Mã nguyên liệu */
    private String materialCode;

    /** Tên nguyên liệu */
    private String materialName;

    /** Tên quốc tế */
    private String internationalName;

    /** Đơn vị tính */
    private String unitOfMeasure;

    /** Loại nguyên liệu */
    private String materialCategory;

    /** Dạng nguyên liệu */
    private String materialFormType;

    // ========== THÔNG TIN LÔ ==========

    /** Mã lô NSX */
    private String batchNumber;

    /** Mã lô nội bộ */
    private String internalBatchCode;

    /** Mã lô của hãng */
    private String manufacturerBatchNumber;

    /** Ngày sản xuất */
    private LocalDate manufacturingDate;

    /** Hạn sử dụng */
    private LocalDate expiryDate;

    /** Ngày nhập */
    private LocalDate receivedDate;

    /** Số lượng nhập */
    private BigDecimal receivedQuantity;

    /** Số lượng hiện tại */
    private BigDecimal currentQuantity;

    /** Số lượng khả dụng */
    private BigDecimal availableQuantity;

    /** Đơn giá */
    private BigDecimal unitPrice;

    /** % Thuế */
    private BigDecimal taxPercent;

    /** Tổng tiền */
    private BigDecimal totalAmount;

    // ========== NHÀ CUNG CẤP & SẢN XUẤT ==========

    /** ID nhà cung cấp */
    private Long supplierId;

    /** Tên nhà cung cấp */
    private String supplierName;

    /** ID nhà sản xuất */
    private Long manufacturerId;

    /** Tên nhà sản xuất */
    private String manufacturerName;

    /** Xuất xứ */
    private String countryOfOrigin;

    /** Số hóa đơn */
    private String invoiceNumber;

    // ========== VỊ TRÍ & FILE ==========

    /** ID vị trí kho */
    private Long locationId;

    /** Mã vị trí kho */
    private String locationCode;

    /** Tên vị trí kho */
    private String locationName;

    /** Tên kho */
    private String warehouseName;

    /** Vị trí kệ */
    private String shelfLocation;

    /** Đường dẫn file COA */
    private String coaFilePath;

    /** Đường dẫn file ảnh */
    private String imagePath;

    /** Đường dẫn file MSDS */
    private String msdsFilePath;

    /** Đường dẫn file chứng nhận kiểm nghiệm */
    private String testCertificatePath;

    // ========== TRẠNG THÁI ==========

    /** Tình trạng kiểm nghiệm (enum) */
    private String testStatus;

    /** Tên hiển thị trạng thái kiểm nghiệm */
    private String testStatusDisplay;

    /** Trạng thái sử dụng (enum) */
    private String usageStatus;

    /** Tên hiển thị trạng thái sử dụng */
    private String usageStatusDisplay;

    // ========== HOẠT CHẤT & KIỂM NGHIỆM ==========

    /** DEPRECATED: Danh sách hoạt chất đã chuyển sang MaterialBatchItemActiveIngredient */
    // private List<MaterialBatchActiveIngredientDTO> batchActiveIngredients;

    /** Trạng thái kiểm nghiệm tổng thể: "Đạt" / "Không đạt" / "Chưa có dữ liệu" */
    private String overallQualificationStatus;

    /** Có tất cả hoạt chất đạt chuẩn không */
    private Boolean isAllQualified;

    /** Danh sách tên hoạt chất không đạt */
    private List<String> unqualifiedIngredients;

    // ========== THÔNG TIN KHÁC ==========

    /** Số COA */
    private String coaNumber;

    /** Số báo cáo kiểm nghiệm */
    private String testReportNumber;

    /** Lý do cách ly */
    private String quarantineReason;

    /** Ghi chú */
    private String notes;

    /** Ngày tạo */
    private LocalDate createdAt;

    /** Người tạo */
    private String createdBy;

    /** Ngày cập nhật */
    private LocalDate updatedAt;

    /** Người cập nhật */
    private String updatedBy;
}
