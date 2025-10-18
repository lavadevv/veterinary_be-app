package ext.vnua.veterinary_beapp.modules.pricing.repository;

import ext.vnua.veterinary_beapp.modules.pricing.model.ProductPricingLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductPricingLineRepository extends JpaRepository<ProductPricingLine, Long> {
    List<ProductPricingLine> findByProductIdOrderBySttAscIdAsc(Long productId);
    void deleteByProductId(Long productId);
}
