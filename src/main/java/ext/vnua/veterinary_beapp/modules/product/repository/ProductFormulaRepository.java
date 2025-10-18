package ext.vnua.veterinary_beapp.modules.product.repository;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormula;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductFormulaRepository extends JpaRepository<ProductFormula, Long>, JpaSpecificationExecutor<ProductFormula> {

    @Query("""
        select pf from ProductFormula pf
        join fetch pf.product p
        where p.id = :productId
        order by pf.createdDate desc
    """)
    List<ProductFormula> findByProductIdWithProductOrderByCreatedDateDesc(@Param("productId") Long productId);

    @Query("""
        select pf from ProductFormula pf
        join fetch pf.product p
        where p.id = :productId and pf.isActive = true
        order by pf.createdDate desc
    """)
    Optional<ProductFormula> findFirstActiveByProductIdWithProduct(@Param("productId") Long productId);

    Optional<ProductFormula> findByProductIdAndVersion(Long productId, String version);

    @Override
    @EntityGraph(attributePaths = { "product" /*, "formulaItems" */ })
    Page<ProductFormula> findAll(Specification<ProductFormula> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"product","formulaItems","formulaItems.material"})
    Optional<ProductFormula> findById(Long id);
}
