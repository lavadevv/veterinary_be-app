// ext/vnua/veterinary_beapp/modules/pcost/repository/EnergyTariffRepository.java
package ext.vnua.veterinary_beapp.modules.pcost.repository;

import ext.vnua.veterinary_beapp.modules.pcost.model.EnergyTariff;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EnergyTariffRepository extends JpaRepository<EnergyTariff, Long>, JpaSpecificationExecutor<EnergyTariff> {
    
    @EntityGraph(attributePaths = {"unitOfMeasure"})
    Optional<EnergyTariff> findByCode(String code);
    
    boolean existsByCode(String code);
    
    @EntityGraph(attributePaths = {"unitOfMeasure"})
    @Query("SELECT et FROM EnergyTariff et WHERE et.isActive = true ORDER BY et.effectiveDate DESC")
    List<EnergyTariff> findByIsActiveTrueOrderByEffectiveDateDesc();
    
    @EntityGraph(attributePaths = {"unitOfMeasure"})
    Optional<EnergyTariff> findById(Long id);
    
    @EntityGraph(attributePaths = {"unitOfMeasure"})
    List<EnergyTariff> findAll();
}
