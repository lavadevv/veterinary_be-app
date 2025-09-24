package ext.vnua.veterinary_beapp.modules.product.repository;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductFormulaRepository extends JpaRepository<ProductFormula, Long> {

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
}
