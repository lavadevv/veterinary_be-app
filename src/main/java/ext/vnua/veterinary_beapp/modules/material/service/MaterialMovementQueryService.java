// File: ext/vnua/veterinary_beapp/modules/material/service/MaterialMovementQueryService.java
package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.model.MaterialMovement;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialMovementQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface MaterialMovementQueryService {
    Page<MaterialMovement> search(CustomMaterialMovementQuery.MovementFilterParam param, PageRequest pageRequest);
}
