package ext.vnua.veterinary_beapp.modules.audits.dto.request;

import ext.vnua.veterinary_beapp.modules.audits.repository.CustomAuditLogQuery;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class AuditLogSearchRequest extends CustomAuditLogQuery.AuditLogFilterParam{
    @Min(value = 0, message = "Số trang phải bắt đầu từ 0")
    private int start = 0;
    @Range(min = 5, max = 50, message = "Số lượng phải từ 5 đến 50")
    private int limit = 10;
}
