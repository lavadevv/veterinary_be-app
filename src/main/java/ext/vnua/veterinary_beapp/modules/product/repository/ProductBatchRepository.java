package ext.vnua.veterinary_beapp.modules.product.repository;

import ext.vnua.veterinary_beapp.modules.product.model.ProductBatch;
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

public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long>, JpaSpecificationExecutor<ProductBatch> {
    Optional<ProductBatch> findByBatchNumber(String batchNumber);
    boolean existsByBatchNumber(String batchNumber);
    @Query("""
        select pb.batchNumber
        from ProductBatch pb
        where pb.product.id = :productId
          and pb.manufacturingDate = :mfgDate
    """)
    List<String> findBatchNumbersByProductAndDate(@Param("productId") Long productId,
                                                  @Param("mfgDate") java.time.LocalDate mfgDate);

    @Override
    @EntityGraph(attributePaths = { "product", "formula", "location" })
    Page<ProductBatch> findAll(Specification<ProductBatch> spec, Pageable pageable);

    // Tùy chọn: để getById cũng an toàn
    @EntityGraph(attributePaths = { "product", "formula", "location" })
    Optional<ProductBatch> findById(Long id);
}