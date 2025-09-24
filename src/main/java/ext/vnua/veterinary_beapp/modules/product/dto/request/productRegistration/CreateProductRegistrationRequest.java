package ext.vnua.veterinary_beapp.modules.product.dto.request.productRegistration;

import ext.vnua.veterinary_beapp.modules.product.enums.RegistrationStatus;
import ext.vnua.veterinary_beapp.modules.product.enums.RegulatoryAuthority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateProductRegistrationRequest {
    @NotNull private Long productId;

    @NotBlank private String registrationNumber;
    private String circulationPermitNumber;

    @NotNull private RegulatoryAuthority regulatoryAuthority;

    @NotNull private LocalDate registrationDate;
    @NotNull private LocalDate effectiveDate;
    @NotNull private LocalDate expiryDate;

    private RegistrationStatus status = RegistrationStatus.ACTIVE;
    @NotBlank private String registrantCompany;
    @NotBlank private String manufacturerCompany;
    private String registrationFilePath;
    private Integer renewalReminderDays = 90;
    private String notes;
}