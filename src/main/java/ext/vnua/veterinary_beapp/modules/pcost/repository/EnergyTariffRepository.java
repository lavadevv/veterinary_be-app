// ext/vnua/veterinary_beapp/modules/pcost/repository/EnergyTariffRepository.java
package ext.vnua.veterinary_beapp.modules.pcost.repository;

import ext.vnua.veterinary_beapp.modules.pcost.model.EnergyTariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface EnergyTariffRepository extends JpaRepository<EnergyTariff, Long>, JpaSpecificationExecutor<EnergyTariff> {
    Optional<EnergyTariff> findByCode(String code);
    boolean existsByCode(String code);
    List<EnergyTariff> findByIsActiveTrueOrderByEffectiveDateDesc();

}
