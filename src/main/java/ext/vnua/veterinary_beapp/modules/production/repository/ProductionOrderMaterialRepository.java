package ext.vnua.veterinary_beapp.modules.production.repository;

import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrderMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductionOrderMaterialRepository
        extends JpaRepository<ProductionOrderMaterial, Long>, JpaSpecificationExecutor<ProductionOrderMaterial> {
}
