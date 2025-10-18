package ext.vnua.veterinary_beapp.modules.pricing.repository;

import ext.vnua.veterinary_beapp.modules.pricing.model.ProductPricingConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductPricingConfigRepository extends JpaRepository<ProductPricingConfig, Long> {
}
