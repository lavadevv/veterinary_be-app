package ext.vnua.veterinary_beapp.modules.productionplan.repository;

import ext.vnua.veterinary_beapp.modules.productionplan.model.ProductionLot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductionLotRepository extends JpaRepository<ProductionLot, Long>, JpaSpecificationExecutor<ProductionLot> {

    @Query("select coalesce(max(l.sequenceInMonth), 0) from ProductionLot l where l.planYear = :year and l.planMonth = :month")
    int findMaxSequenceInMonth(@Param("year") int year, @Param("month") int month);

    /**
     * Find lot with plans and formulas (for list view)
     * NOTE: Don't fetch productLines here to avoid MultipleBagFetchException
     */
    @EntityGraph(attributePaths = {"plans", "plans.formula", "plans.formula.header"})
    Page<ProductionLot> findAll(Specification<ProductionLot> spec, Pageable pageable);

    /**
     * Find lot with plans only (step 1 for detail view)
     * We'll fetch productLines separately in service to avoid MultipleBagFetchException
     */
    @Query("SELECT DISTINCT l FROM ProductionLot l " +
           "LEFT JOIN FETCH l.plans p " +
           "LEFT JOIN FETCH p.formula f " +
           "LEFT JOIN FETCH f.header h " +
           "WHERE l.id = :id")
    Optional<ProductionLot> findByIdWithPlansAndFormulas(@Param("id") Long id);

    /**
     * Find lot with plans and product lines (will be used with IN clause to avoid N+1)
     * This is step 2 for detail view
     */
    @Query("SELECT DISTINCT p FROM ProductionPlan p " +
           "LEFT JOIN FETCH p.productLines pl " +
           "LEFT JOIN FETCH pl.product prod " +
           "LEFT JOIN FETCH pl.productionCostSheet cs " +
           "WHERE p.lot.id = :lotId")
    java.util.List<ext.vnua.veterinary_beapp.modules.productionplan.model.ProductionPlan> findPlansByLotIdWithProductLines(@Param("lotId") Long lotId);
}

