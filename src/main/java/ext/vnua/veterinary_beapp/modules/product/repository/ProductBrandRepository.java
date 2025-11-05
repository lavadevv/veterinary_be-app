package ext.vnua.veterinary_beapp.modules.product.repository;

import ext.vnua.veterinary_beapp.modules.product.model.ProductBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductBrandRepository extends JpaRepository<ProductBrand, Long>, JpaSpecificationExecutor<ProductBrand> {
    
    /**
     * Tìm ProductBrand theo product và brand
     */
    @Query("SELECT pb FROM ProductBrand pb WHERE pb.product.id = :productId AND pb.brand.id = :brandId")
    Optional<ProductBrand> findByProductIdAndBrandId(@Param("productId") Long productId, @Param("brandId") Long brandId);

    /**
     * Danh sách ProductBrand theo product
     */
    @Query("SELECT pb FROM ProductBrand pb WHERE pb.product.id = :productId")
    List<ProductBrand> findByProductId(@Param("productId") Long productId);

    /**
     * Danh sách ProductBrand theo brand
     */
    @Query("SELECT pb FROM ProductBrand pb WHERE pb.brand.id = :brandId")
    List<ProductBrand> findByBrandId(@Param("brandId") Long brandId);

    /**
     * Danh sách ProductBrand theo ProductionCostSheet
     */
    @Query("SELECT pb FROM ProductBrand pb WHERE pb.productionCostSheet.id = :sheetId")
    List<ProductBrand> findByProductionCostSheetId(@Param("sheetId") Long sheetId);

    /**
     * Kiểm tra tồn tại product-brand
     */
    @Query("SELECT COUNT(pb) > 0 FROM ProductBrand pb WHERE pb.product.id = :productId AND pb.brand.id = :brandId")
    boolean existsByProductIdAndBrandId(@Param("productId") Long productId, @Param("brandId") Long brandId);

    /**
     * Lấy danh sách ProductBrand active theo product
     */
    @Query("SELECT pb FROM ProductBrand pb WHERE pb.product.id = :productId AND pb.isActive = true")
    List<ProductBrand> findActiveByProductId(@Param("productId") Long productId);

    /**
     * Lấy danh sách ProductBrand active theo brand
     */
    @Query("SELECT pb FROM ProductBrand pb WHERE pb.brand.id = :brandId AND pb.isActive = true")
    List<ProductBrand> findActiveByBrandId(@Param("brandId") Long brandId);
}
