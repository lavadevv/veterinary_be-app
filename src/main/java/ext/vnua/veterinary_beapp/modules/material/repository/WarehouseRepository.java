package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse,Long>, JpaSpecificationExecutor<Warehouse> {
    // Find by warehouse code
    Optional<Warehouse> findByWarehouseCode(String warehouseCode);

    // Find all active warehouses
    List<Warehouse> findByIsActiveTrue();

    // Find by warehouse type and active status
    List<Warehouse> findByWarehouseTypeAndIsActiveTrue(String warehouseType);

    // Check duplicate warehouse code (excluding current ID for update)
    Optional<Warehouse> findByWarehouseCodeAndIdNot(String warehouseCode, Long id);
}
