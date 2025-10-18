package ext.vnua.veterinary_beapp.modules.pcost.repository;

import ext.vnua.veterinary_beapp.modules.pcost.model.LaborRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface LaborRateRepository extends JpaRepository<LaborRate, Long>, JpaSpecificationExecutor<LaborRate> {
    Optional<LaborRate> findByCode(String code);
    List<LaborRate> findByIsActiveTrueOrderByEffectiveDateDesc();
}
