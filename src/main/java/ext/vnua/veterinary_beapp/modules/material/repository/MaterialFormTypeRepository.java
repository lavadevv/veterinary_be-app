// File: ext/vnua/veterinary_beapp/modules/material/repository/MaterialFormTypeRepository.java
package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.MaterialFormType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MaterialFormTypeRepository extends JpaRepository<MaterialFormType, Long>, JpaSpecificationExecutor<MaterialFormType> {

    Optional<MaterialFormType> findByName(String name);

    Optional<MaterialFormType> findByNameAndIdNot(String name, Long id);

    boolean existsByNameAndIdNot(String name, Long id);
    
    List<MaterialFormType> findAllByOrderByNameAsc();
}
