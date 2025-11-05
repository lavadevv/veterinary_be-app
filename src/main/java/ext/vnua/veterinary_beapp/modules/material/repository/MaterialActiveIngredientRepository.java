package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.MaterialActiveIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialActiveIngredientRepository extends JpaRepository<MaterialActiveIngredient, Long>, JpaSpecificationExecutor<MaterialActiveIngredient> {

    // Tìm theo material ID
    List<MaterialActiveIngredient> findByMaterialId(Long materialId);

    // Tìm theo active ingredient ID
    List<MaterialActiveIngredient> findByActiveIngredientId(Long activeIngredientId);

    // Tìm theo material ID và active ingredient ID
    Optional<MaterialActiveIngredient> findByMaterialIdAndActiveIngredientId(Long materialId, Long activeIngredientId);

    // Kiểm tra xem material đã có active ingredient này chưa
    boolean existsByMaterialIdAndActiveIngredientId(Long materialId, Long activeIngredientId);

    // Xóa tất cả active ingredients của một material
    @Modifying
    @Query("DELETE FROM MaterialActiveIngredient mai WHERE mai.material.id = :materialId")
    void deleteByMaterialId(@Param("materialId") Long materialId);

    // Xóa theo material ID và active ingredient ID
    @Modifying
    @Query("DELETE FROM MaterialActiveIngredient mai WHERE mai.material.id = :materialId AND mai.activeIngredient.id = :activeIngredientId")
    void deleteByMaterialIdAndActiveIngredientId(@Param("materialId") Long materialId, @Param("activeIngredientId") Long activeIngredientId);

    // Lấy danh sách materials sử dụng active ingredient cụ thể
    @Query("SELECT mai FROM MaterialActiveIngredient mai " +
           "JOIN FETCH mai.material m " +
           "JOIN FETCH mai.activeIngredient ai " +
           "WHERE ai.id = :activeIngredientId AND m.isActive = true")
    List<MaterialActiveIngredient> findMaterialsUsingActiveIngredient(@Param("activeIngredientId") Long activeIngredientId);

    // Đếm số lượng materials sử dụng active ingredient
    @Query("SELECT COUNT(mai) FROM MaterialActiveIngredient mai " +
           "WHERE mai.activeIngredient.id = :activeIngredientId")
    long countMaterialsUsingActiveIngredient(@Param("activeIngredientId") Long activeIngredientId);

    // Lấy tất cả active ingredients của một material với eager loading
    @Query("SELECT mai FROM MaterialActiveIngredient mai " +
           "JOIN FETCH mai.activeIngredient ai " +
           "WHERE mai.material.id = :materialId " +
           "ORDER BY ai.ingredientName")
    List<MaterialActiveIngredient> findByMaterialIdWithActiveIngredient(@Param("materialId") Long materialId);
}