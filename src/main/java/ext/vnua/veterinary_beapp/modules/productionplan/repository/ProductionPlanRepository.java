package ext.vnua.veterinary_beapp.modules.productionplan.repository;

import ext.vnua.veterinary_beapp.modules.productionplan.model.ProductionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Long>, JpaSpecificationExecutor<ProductionPlan> {

    @EntityGraph(attributePaths = {
            "lot",
            "formula",
            "formula.header",
            "productLines",
            "productLines.product",
            "productLines.productionCostSheet"
    })
    Optional<ProductionPlan> findById(Long id);

    /**
     * Fetch plans for listing with required associations to avoid N+1 and Lazy issues when mapping to DTO.
     */
    @Override
    @EntityGraph(attributePaths = {
            "lot",
            "formula",
            "formula.header",
            "productLines",
            "productLines.product",
            "productLines.productionCostSheet"
    })
    Page<ProductionPlan> findAll(Specification<ProductionPlan> spec, Pageable pageable);
    
    /**
     * Calculate total planned quantity for a lot
     */
    @Query("SELECT COALESCE(SUM(pl.plannedQuantity), 0) FROM ProductionPlanProduct pl " +
           "WHERE pl.plan.lot.id = :lotId")
    java.math.BigDecimal sumPlannedQuantityByLotId(@Param("lotId") Long lotId);
}
