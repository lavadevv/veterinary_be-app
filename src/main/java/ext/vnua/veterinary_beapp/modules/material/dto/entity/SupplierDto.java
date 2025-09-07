package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SupplierDto {
    private Long id;
    private String supplierCode;
    private String supplierName;
    private String manufacturerName;
    private String distributorName;
    private String address;
    private String registrationNumber;
    private String phone;
    private String email;
    private String contactPerson;
    private String gmpCertificate;
    private LocalDate gmpExpiryDate;
    private String countryOfOrigin;
    private Boolean isActive;
    private String notes;

    @Override
    public String toString() {
        return String.format(
                "Nhà cung cấp:\n" +
                        "   - ID: %d\n" +
                        "   - Mã: %s\n" +
                        "   - Tên: %s\n" +
                        "   - Nhà sản xuất: %s\n" +
                        "   - Nhà phân phối: %s\n" +
                        "   - Địa chỉ: %s\n" +
                        "   - Điện thoại: %s\n" +
                        "   - Email: %s\n" +
                        "   - Người liên hệ: %s\n" +
                        "   - Giấy chứng nhận GMP: %s (hết hạn: %s)\n" +
                        "   - Xuất xứ: %s\n" +
                        "   - Trạng thái: %s\n",
                id,
                supplierCode,
                supplierName,
                manufacturerName,
                distributorName,
                address,
                phone,
                email,
                contactPerson,
                gmpCertificate,
                gmpExpiryDate != null ? gmpExpiryDate.toString() : "Không rõ",
                countryOfOrigin,
                Boolean.TRUE.equals(isActive) ? "Đang hợp tác" : "Ngừng hợp tác"
        );
    }

}
