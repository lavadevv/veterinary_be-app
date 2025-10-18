// request
package ext.vnua.veterinary_beapp.modules.reports.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MaterialCostVarianceRequest {
    @NotNull @Min(2000) @Max(3000) private Integer year;
    @NotNull @Min(1) @Max(12) private Integer month;
}
