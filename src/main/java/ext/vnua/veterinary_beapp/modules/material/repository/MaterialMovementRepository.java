// sửa file ext/vnua/veterinary_beapp/modules/material/repository/MaterialMovementRepository.java
package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.MaterialMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MaterialMovementRepository extends JpaRepository<MaterialMovement, Long>,
        JpaSpecificationExecutor<MaterialMovement> {
}
