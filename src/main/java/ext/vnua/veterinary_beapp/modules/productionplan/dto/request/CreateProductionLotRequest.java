package ext.vnua.veterinary_beapp.modules.productionplan.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateProductionLotRequest {

    private LocalDate planDate; // common plan date for the lot
    private String notes;       // common notes for the lot

    @Valid
    @NotEmpty
    private List<CreateProductionPlanRequest> plans; // each item describes one plan (formula + products)
}

