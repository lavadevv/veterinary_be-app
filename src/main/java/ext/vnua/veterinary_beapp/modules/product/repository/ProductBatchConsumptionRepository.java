package ext.vnua.veterinary_beapp.modules.product.repository;

import ext.vnua.veterinary_beapp.modules.product.model.ProductBatchConsumption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductBatchConsumptionRepository extends JpaRepository<ProductBatchConsumption, Long> {
    List<ProductBatchConsumption> findByProductBatchId(Long batchId);
}