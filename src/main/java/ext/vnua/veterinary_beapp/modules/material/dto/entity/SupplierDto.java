package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import lombok.Data;

@Data
public class SupplierDto {
    private Long id;
    private String supplierCode;
    private String supplierName;

    // thay vì manufacturerName / distributorName => chỉ trả id + optional name để FE hiển thị
    private Long manufacturerId;
    private String manufacturerName; // optional, tiện hiển thị (map từ entity nếu có)

    private String address;
    private String registrationNumber;
    private String phone;
    private String email;
    private String contactPerson;
    private String gmpCertificate;
    private java.time.LocalDate gmpExpiryDate;
    private String countryOfOrigin;
    private Boolean isActive;
    private String notes;
}
