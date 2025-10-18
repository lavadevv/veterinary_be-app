// ext/vnua/veterinary_beapp/modules/pcost/repository/ProductionCostSheetRepository.java
package ext.vnua.veterinary_beapp.modules.pcost.repository;

import ext.vnua.veterinary_beapp.modules.pcost.model.ProductionCostSheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductionCostSheetRepository extends JpaRepository<ProductionCostSheet, Long>, JpaSpecificationExecutor<ProductionCostSheet> {
    @EntityGraph(attributePaths = "items")
    Optional<ProductionCostSheet> findBySheetCode(String sheetCode);

    @EntityGraph(attributePaths = "items")
    List<ProductionCostSheet> findByProductIdOrderByEffectiveDateDesc(Long productId);

    @EntityGraph(attributePaths = "items")
    Page<ProductionCostSheet> findAll(Specification<ProductionCostSheet> spec, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Optional<ProductionCostSheet> findById(Long id);
}
