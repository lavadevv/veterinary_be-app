package ext.vnua.veterinary_beapp.modules.product.repository;

import ext.vnua.veterinary_beapp.modules.product.model.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long>, JpaSpecificationExecutor<ProductBatch> {
    Optional<ProductBatch> findByBatchNumber(String batchNumber);
    boolean existsByBatchNumber(String batchNumber);

}