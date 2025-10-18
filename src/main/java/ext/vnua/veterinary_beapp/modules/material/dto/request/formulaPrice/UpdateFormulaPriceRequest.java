package ext.vnua.veterinary_beapp.modules.material.dto.request.formulaPrice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateFormulaPriceRequest {
    @NotNull
    private Long materialId;
    @NotNull @DecimalMin("0.0")
    private BigDecimal newPrice; // VNĐ theo UOM của material
    private String note; // optional
}