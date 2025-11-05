package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.WarehouseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WarehouseTypeRepository
        extends JpaRepository<WarehouseType, Long>, JpaSpecificationExecutor<WarehouseType> {
}
