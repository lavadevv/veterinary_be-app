package ext.vnua.veterinary_beapp.modules.product.dto.request.productRegistration;

import ext.vnua.veterinary_beapp.modules.product.enums.RegistrationStatus;
import ext.vnua.veterinary_beapp.modules.product.enums.RegulatoryAuthority;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProductRegistrationRequest {
    @NotNull private Long id;
    @NotNull private Long productId;

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