// ext/vnua/veterinary_beapp/modules/pcost/service/EnergyTariffService.java
package ext.vnua.veterinary_beapp.modules.pcost.service;

import ext.vnua.veterinary_beapp.modules.pcost.model.EnergyTariff;
import ext.vnua.veterinary_beapp.modules.pcost.repository.custom.CustomEnergyTariffQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface EnergyTariffService {
    EnergyTariff create(EnergyTariff r);
    EnergyTariff update(Long id, EnergyTariff r);
    EnergyTariff get(Long id);
    void delete(Long id);

    List<EnergyTariff> listActive();

    // NEW
    Page<EnergyTariff> search(CustomEnergyTariffQuery.EnergyTariffFilterParam param, PageRequest pageRequest);
}
