package ext.vnua.veterinary_beapp.modules.production.dto.request.productionLine;

import ext.vnua.veterinary_beapp.modules.production.constants.ProductionLineConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProductionLineRequest {

    @NotBlank(message = ProductionLineConstants.ValidationMessages.LINE_CODE_REQUIRED)
    @Size(max = 50, message = "Mã dây chuyền không được vượt quá 50 ký tự")
    private String lineCode;

    @NotBlank(message = ProductionLineConstants.ValidationMessages.LINE_NAME_REQUIRED)
    @Size(max = 255, message = "Tên dây chuyền không được vượt quá 255 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
}