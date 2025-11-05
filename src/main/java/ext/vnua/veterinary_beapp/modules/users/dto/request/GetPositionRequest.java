package ext.vnua.veterinary_beapp.modules.users.dto.request;

import ext.vnua.veterinary_beapp.modules.users.repository.CustomOrgQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class GetPositionRequest extends CustomOrgQuery.OrgFilterParam {
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;

    @Range(min = 5, max = 200, message = "Số lượng chức vụ trong một trang là từ 5 đến 200")
    private int limit = 20;
}
