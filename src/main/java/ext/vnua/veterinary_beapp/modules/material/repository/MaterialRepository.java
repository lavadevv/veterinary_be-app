package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.Material;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long>, JpaSpecificationExecutor<Material> {

     // For pagination - includes activeIngredients for accurate count
     // Note: May trigger HHH90003004 warning but necessary for activeIngredientsCount accuracy
     @Override
     @EntityGraph(attributePaths = { 
         "supplier", "supplier.manufacturer", "unitOfMeasure", 
         "materialCategory", "materialFormType", "activeIngredients", "activeIngredients.activeIngredient" 
     })
     Page<Material> findAll(@Nullable Specification<Material> spec, Pageable pageable);

    // For non-paginated lists - can include collections
    @Override
    @EntityGraph(attributePaths = { 
        "supplier", "supplier.manufacturer", "unitOfMeasure", 
        "materialCategory", "materialFormType", "activeIngredients", "activeIngredients.activeIngredient" 
    })
    java.util.List<Material> findAll(@Nullable Specification<Material> spec);

    @EntityGraph(attributePaths = { 
        "supplier", "supplier.manufacturer", "unitOfMeasure", 
        "materialCategory", "materialFormType", "activeIngredients", "activeIngredients.activeIngredient" 
    })
    List<Material> findAll();

    @EntityGraph(attributePaths = { 
        "supplier", "supplier.manufacturer", "unitOfMeasure", 
        "materialCategory", "materialFormType", "activeIngredients", "activeIngredients.activeIngredient" 
    })
    Optional<Material> findById(Long id);

    // Separate method for loading material with active ingredients (no pagination)
    @EntityGraph(attributePaths = { 
        "supplier", "supplier.manufacturer", "unitOfMeasure", 
        "materialCategory", "materialFormType", "activeIngredients", "activeIngredients.activeIngredient" 
    })
    @Query("SELECT m FROM Material m WHERE m.id = :id")
    Optional<Material> findByIdWithActiveIngredients(@Param("id") Long id);

    // Tìm theo mã vật liệu
    @EntityGraph(attributePaths = { 
        "supplier", "supplier.manufacturer", "unitOfMeasure", 
        "materialCategory", "materialFormType", "activeIngredients", "activeIngredients.activeIngredient" 
    })
    Optional<Material> findByMaterialCode(String materialCode);

    // Tìm theo tên vật liệu  
    @EntityGraph(attributePaths = { 
        "supplier", "supplier.manufacturer", "unitOfMeasure", 
        "materialCategory", "materialFormType", "activeIngredients", "activeIngredients.activeIngredient" 
    })
    Optional<Material> findByMaterialName(String materialName);

    // Tìm theo nhà cung cấp
    @EntityGraph(attributePaths = { 
        "supplier", "supplier.manufacturer", "unitOfMeasure", 
        "materialCategory", "materialFormType", "activeIngredients", "activeIngredients.activeIngredient" 
    })
    List<Material> findBySupplierId(Long supplierId);

    // Tìm vật liệu đang hoạt động
    @EntityGraph(attributePaths = { 
        "supplier", "supplier.manufacturer", "unitOfMeasure", 
        "materialCategory", "materialFormType", "activeIngredients", "activeIngredients.activeIngredient" 
    })
    List<Material> findByIsActiveTrue();

    // Tìm vật liệu có tồn kho thấp
    @EntityGraph(attributePaths = { 
        "supplier", "supplier.manufacturer", "unitOfMeasure", 
        "materialCategory", "materialFormType", "activeIngredients", "activeIngredients.activeIngredient" 
    })
    @Query("SELECT m FROM Material m WHERE m.currentStock <= m.minimumStockLevel AND m.isActive = true")
    List<Material> findLowStockMaterials();

    // Tìm vật liệu cần bảo quản lạnh
    @EntityGraph(attributePaths = { 
        "supplier", "supplier.manufacturer", "unitOfMeasure", 
        "materialCategory", "materialFormType", "activeIngredients", "activeIngredients.activeIngredient" 
    })
    List<Material> findByRequiresColdStorageTrue();

    // Kiểm tra mã vật liệu trùng (loại trừ ID hiện tại - dùng cho update)
    @EntityGraph(attributePaths = { 
        "supplier", "supplier.manufacturer", "unitOfMeasure", 
        "materialCategory", "materialFormType", "activeIngredients", "activeIngredients.activeIngredient" 
    })
    Optional<Material> findByMaterialCodeAndIdNot(String materialCode, Long id);

    // Kiểm tra tên vật liệu trùng (loại trừ ID hiện tại - dùng cho update)
    @EntityGraph(attributePaths = { 
        "supplier", "supplier.manufacturer", "unitOfMeasure", 
        "materialCategory", "materialFormType", "activeIngredients", "activeIngredients.activeIngredient" 
    })
    Optional<Material> findByMaterialNameAndIdNot(String materialName, Long id);

    // Tìm vật liệu có giá trong khoảng (BigDecimal)
    @EntityGraph(attributePaths = { 
        "supplier", "supplier.manufacturer", "unitOfMeasure", 
        "materialCategory", "materialFormType", "activeIngredients", "activeIngredients.activeIngredient" 
    })
    @Query("SELECT m FROM Material m WHERE m.fixedPrice BETWEEN :minPrice AND :maxPrice AND m.isActive = true")
    List<Material> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    // Cập nhật tồn kho trực tiếp (ít dùng vì đã có sync) — cần @Modifying
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Material m SET m.currentStock = :newStock WHERE m.id = :materialId")
    int updateCurrentStock(@Param("materialId") Long materialId, @Param("newStock") BigDecimal newStock);
}
