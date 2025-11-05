// File: ext/vnua/veterinary_beapp/modules/material/dto/request/materialCategory/GetMaterialCategoryRequest.java
package ext.vnua.veterinary_beapp.modules.material.dto.request.materialCategory;

import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialCategoryQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetMaterialCategoryRequest extends CustomMaterialCategoryQuery.MaterialCategoryQuery.CategoryFilterParam {

    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;

    @Range(min = 3, max = 50, message = "Số lượng bản ghi trong một trang là từ 3 đến 50")
    private int limit = 10;
}
