package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.ActiveIngredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActiveIngredientRepository extends JpaRepository<ActiveIngredient, Long>, JpaSpecificationExecutor<ActiveIngredient> {

    // Tìm theo mã hoạt chất
    Optional<ActiveIngredient> findByIngredientCode(String ingredientCode);

    // Tìm theo tên hoạt chất
    Optional<ActiveIngredient> findByIngredientName(String ingredientName);

    // Tìm theo CAS number
    Optional<ActiveIngredient> findByCasNumber(String casNumber);

    // Tìm hoạt chất đang hoạt động
    List<ActiveIngredient> findByIsActiveTrueOrderByIngredientNameAsc();

    // Tìm hoạt chất theo tên (LIKE)
    @Query("SELECT ai FROM ActiveIngredient ai WHERE ai.ingredientName LIKE %:name% AND ai.isActive = true ORDER BY ai.ingredientName")
    List<ActiveIngredient> findByIngredientNameContainingIgnoreCaseAndIsActiveTrue(@Param("name") String name);

    // Kiểm tra trùng mã hoạt chất (loại trừ ID hiện tại)
    Optional<ActiveIngredient> findByIngredientCodeAndIdNot(String ingredientCode, Long id);

    // Kiểm tra trùng tên hoạt chất (loại trừ ID hiện tại)
    Optional<ActiveIngredient> findByIngredientNameAndIdNot(String ingredientName, Long id);

    // Kiểm tra trùng CAS number (loại trừ ID hiện tại)
    Optional<ActiveIngredient> findByCasNumberAndIdNot(String casNumber, Long id);

    // Tìm kiếm hoạt chất theo từ khóa
    @Query("SELECT ai FROM ActiveIngredient ai WHERE " +
           "(LOWER(ai.ingredientName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ai.ingredientCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ai.casNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "ai.isActive = true ORDER BY ai.ingredientName")
    List<ActiveIngredient> searchByKeyword(@Param("keyword") String keyword);

    // Phân trang tìm kiếm
    @Query("SELECT ai FROM ActiveIngredient ai WHERE " +
           "(LOWER(ai.ingredientName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ai.ingredientCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ai.casNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ActiveIngredient> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Kiểm tra xem mã đã tồn tại chưa
    boolean existsByIngredientCode(String ingredientCode);

    // Kiểm tra xem tên đã tồn tại chưa
    boolean existsByIngredientName(String ingredientName);

    // Kiểm tra xem CAS number đã tồn tại chưa
    boolean existsByCasNumber(String casNumber);

    // Kiểm tra trùng với loại trừ ID
    boolean existsByIngredientCodeAndIdNot(String ingredientCode, Long id);
    boolean existsByIngredientNameAndIdNot(String ingredientName, Long id);
    boolean existsByCasNumberAndIdNot(String casNumber, Long id);
}