package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatchItemActiveIngredient;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatchItem;
import ext.vnua.veterinary_beapp.modules.material.model.ActiveIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialBatchItemActiveIngredientRepository extends JpaRepository<MaterialBatchItemActiveIngredient, Long> {

    /**
     * Tìm tất cả hoạt chất của một batch item
     */
    List<MaterialBatchItemActiveIngredient> findByBatchItem(MaterialBatchItem batchItem);

    /**
     * Tìm tất cả hoạt chất của một batch item theo id
     */
    List<MaterialBatchItemActiveIngredient> findByBatchItemId(Long batchItemId);

    /**
     * Tìm một hoạt chất cụ thể của một batch item
     */
    Optional<MaterialBatchItemActiveIngredient> findByBatchItemAndActiveIngredient(
            MaterialBatchItem batchItem, ActiveIngredient activeIngredient);

    /**
     * Tìm các batch items chứa một hoạt chất cụ thể
     */
    List<MaterialBatchItemActiveIngredient> findByActiveIngredient(ActiveIngredient activeIngredient);

    /**
     * Tìm các batch items có test content value khác null (đã có kết quả test)
     */
    @Query("SELECT mbiai FROM MaterialBatchItemActiveIngredient mbiai " +
           "WHERE mbiai.batchItem.id = :batchItemId " +
           "AND mbiai.testContentValue IS NOT NULL")
    List<MaterialBatchItemActiveIngredient> findTestedIngredientsByBatchItemId(@Param("batchItemId") Long batchItemId);

    /**
     * Tìm các hoạt chất chưa test (test content value = null)
     */
    @Query("SELECT mbiai FROM MaterialBatchItemActiveIngredient mbiai " +
           "WHERE mbiai.batchItem.id = :batchItemId " +
           "AND mbiai.testContentValue IS NULL")
    List<MaterialBatchItemActiveIngredient> findUntestedIngredientsByBatchItemId(@Param("batchItemId") Long batchItemId);

    /**
     * Tìm các hoạt chất không đạt chuẩn (test value ngoài khoảng COA)
     */
    @Query("SELECT mbiai FROM MaterialBatchItemActiveIngredient mbiai " +
           "WHERE mbiai.batchItem.id = :batchItemId " +
           "AND mbiai.testContentValue IS NOT NULL " +
           "AND (mbiai.testContentValue < mbiai.coaMinValue " +
           "     OR mbiai.testContentValue > mbiai.coaMaxValue)")
    List<MaterialBatchItemActiveIngredient> findUnqualifiedIngredientsByBatchItemId(@Param("batchItemId") Long batchItemId);

    /**
     * Kiểm tra xem batch item đã có hoạt chất này chưa
     */
    boolean existsByBatchItemIdAndActiveIngredientId(Long batchItemId, Long activeIngredientId);

    /**
     * Xóa tất cả hoạt chất của một batch item
     */
    void deleteByBatchItemId(Long batchItemId);

    /**
     * Đếm số lượng hoạt chất của một batch item
     */
    long countByBatchItemId(Long batchItemId);

    /**
     * Tìm các hoạt chất theo batch id (qua batchItem)
     */
    @Query("SELECT mbiai FROM MaterialBatchItemActiveIngredient mbiai " +
           "WHERE mbiai.batchItem.batch.id = :batchId")
    List<MaterialBatchItemActiveIngredient> findByBatchId(@Param("batchId") Long batchId);
}
