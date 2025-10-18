// ext/vnua/veterinary_beapp/modules/pcost/dto/GetEnergyTariffRequest.java
package ext.vnua.veterinary_beapp.modules.pcost.dto;

import ext.vnua.veterinary_beapp.modules.pcost.repository.custom.CustomEnergyTariffQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GetEnergyTariffRequest extends CustomEnergyTariffQuery.EnergyTariffFilterParam {

    @Min(value = 0, message = "Số trang (start) phải bắt đầu từ 0")
    private int start = 0;

    @Min(value = 5, message = "Số bản ghi mỗi trang (limit) tối thiểu là 5")
    @Max(value = 50, message = "Số bản ghi mỗi trang (limit) tối đa là 50")
    private int limit = 20;

    /** Gợi ý: nếu FE không truyền sortType mà truyền sortField thì default = DESC */
    @Pattern(regexp = "ASC|DESC", message = "sortType chỉ nhận ASC hoặc DESC")
    @Override
    public String getSortType() {
        // Nếu null thì trả về null để service áp mặc định DESC theo id
        return super.getSortType();
    }
}
