// File: ext/vnua/veterinary_beapp/modules/material/repository/MaterialCategoryRepository.java
package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.MaterialCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MaterialCategoryRepository extends JpaRepository<MaterialCategory, Long>, JpaSpecificationExecutor<MaterialCategory> {

    Optional<MaterialCategory> findByCategoryName(String categoryName);

    Optional<MaterialCategory> findByCategoryNameAndIdNot(String categoryName, Long id);

    List<MaterialCategory> findAllByOrderByCategoryNameAsc();
}
