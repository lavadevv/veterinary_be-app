// ext/vnua/veterinary_beapp/modules/material/repository/UnitOfMeasureRepository.java
package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long>, JpaSpecificationExecutor<UnitOfMeasure> {
    Optional<UnitOfMeasure> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
