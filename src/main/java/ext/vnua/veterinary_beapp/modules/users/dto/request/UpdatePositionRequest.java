package ext.vnua.veterinary_beapp.modules.users.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePositionRequest {
    @NotNull
    private Long id;

    @Size(max = 255)
    private String name;
}
