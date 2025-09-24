package ext.vnua.veterinary_beapp.modules.product.dto;

import ext.vnua.veterinary_beapp.modules.product.enums.RegistrationStatus;
import ext.vnua.veterinary_beapp.modules.product.enums.RegulatoryAuthority;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProductRegistrationDto {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private String registrationNumber;
    private String circulationPermitNumber;
    private RegulatoryAuthority regulatoryAuthority;
    private LocalDate registrationDate;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private RegistrationStatus status;
    private String registrantCompany;
    private String manufacturerCompany;
    private String registrationFilePath;
    private Integer renewalReminderDays;
    private String notes;
}