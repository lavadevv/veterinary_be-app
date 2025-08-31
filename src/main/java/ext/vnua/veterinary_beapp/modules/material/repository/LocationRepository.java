package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location,Long>, JpaSpecificationExecutor<Location> {

    // Tìm theo mã vị trí
    Optional<Location> findByLocationCode(String locationCode);

    // Tìm theo kho
    List<Location> findByWarehouseId(Long warehouseId);

    // Tìm vị trí còn trống theo kho
    List<Location> findByWarehouseIdAndIsAvailableTrue(Long warehouseId);

    // Kiểm tra mã vị trí trùng trong cùng kho
    Optional<Location> findByLocationCodeAndWarehouseId(String locationCode, Long warehouseId);

    // Kiểm tra mã vị trí trùng trong cùng kho (loại trừ ID hiện tại - dùng cho update)
    Optional<Location> findByLocationCodeAndWarehouseIdAndIdNot(String locationCode, Long warehouseId, Long id);

    // Tìm theo kệ trong kho
    List<Location> findByWarehouseIdAndShelf(Long warehouseId, String shelf);

    // Tìm theo tầng trong kho
    List<Location> findByWarehouseIdAndFloor(Long warehouseId, String floor);

    @Query("SELECT l FROM Location l " +
            "WHERE l.warehouse.id = :warehouseId " +
            "AND l.currentCapacity < l.maxCapacity " +
            "AND l.isAvailable = true")
    List<Location> findAvailableLocations(@Param("warehouseId") Long warehouseId);
}
