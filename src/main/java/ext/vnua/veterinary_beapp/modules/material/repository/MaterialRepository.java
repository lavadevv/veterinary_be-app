package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material,Long>, JpaSpecificationExecutor<Material> {
    // Tìm theo mã vật liệu
    Optional<Material> findByMaterialCode(String materialCode);

    // Tìm theo tên vật liệu
    Optional<Material> findByMaterialName(String materialName);

    // Tìm theo nhà cung cấp
    List<Material> findBySupplierId(Long supplierId);

    // Tìm vật liệu đang hoạt động
    List<Material> findByIsActiveTrue();

    // Tìm vật liệu có tồn kho thấp
    @Query("SELECT m FROM Material m WHERE m.currentStock <= m.minimumStockLevel AND m.isActive = true")
    List<Material> findLowStockMaterials();

    // Tìm vật liệu cần bảo quản lạnh
    List<Material> findByRequiresColdStorageTrue();

    // Kiểm tra mã vật liệu trùng (loại trừ ID hiện tại - dùng cho update)
    Optional<Material> findByMaterialCodeAndIdNot(String materialCode, Long id);

    // Kiểm tra tên vật liệu trùng (loại trừ ID hiện tại - dùng cho update)
    Optional<Material> findByMaterialNameAndIdNot(String materialName, Long id);

    // Tìm vật liệu theo loại
    List<Material> findByMaterialType(ext.vnua.veterinary_beapp.modules.material.enums.MaterialType materialType);

    // Tìm vật liệu theo dạng
    List<Material> findByMaterialForm(ext.vnua.veterinary_beapp.modules.material.enums.MaterialForm materialForm);

    // Tìm vật liệu có giá trong khoảng
    @Query("SELECT m FROM Material m WHERE m.fixedPrice BETWEEN :minPrice AND :maxPrice AND m.isActive = true")
    List<Material> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    // Cập nhật tồn kho
    @Query("UPDATE Material m SET m.currentStock = :newStock WHERE m.id = :materialId")
    void updateCurrentStock(@Param("materialId") Long materialId, @Param("newStock") Double newStock);
}
