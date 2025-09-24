package ext.vnua.veterinary_beapp.modules.production.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApproveProductionOrderRequest {
    @NotNull
    private Long orderId;

    @NotNull
    private Long approvedById;
}
