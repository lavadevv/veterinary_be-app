package ext.vnua.veterinary_beapp.modules.productionplan.repository;

import ext.vnua.veterinary_beapp.modules.productionplan.model.ProductionPlanProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionPlanProductRepository extends JpaRepository<ProductionPlanProduct, Long> {
}
