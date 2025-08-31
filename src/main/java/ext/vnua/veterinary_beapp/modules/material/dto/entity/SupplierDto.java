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
}
