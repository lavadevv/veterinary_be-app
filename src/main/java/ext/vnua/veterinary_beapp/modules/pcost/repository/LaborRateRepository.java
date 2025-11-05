package ext.vnua.veterinary_beapp.modules.pcost.repository;

import ext.vnua.veterinary_beapp.modules.pcost.model.LaborRate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LaborRateRepository extends JpaRepository<LaborRate, Long>, JpaSpecificationExecutor<LaborRate> {
    
    @EntityGraph(attributePaths = {"unitOfMeasure"})
    Optional<LaborRate> findByCode(String code);
    
    @EntityGraph(attributePaths = {"unitOfMeasure"})
    @Query("SELECT lr FROM LaborRate lr WHERE lr.isActive = true ORDER BY lr.effectiveDate DESC")
    List<LaborRate> findByIsActiveTrueOrderByEffectiveDateDesc();
    
    @EntityGraph(attributePaths = {"unitOfMeasure"})
    Optional<LaborRate> findById(Long id);
    
    @EntityGraph(attributePaths = {"unitOfMeasure"})
    List<LaborRate> findAll();
}
