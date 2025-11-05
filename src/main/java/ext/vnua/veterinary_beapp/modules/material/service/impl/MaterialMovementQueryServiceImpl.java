// File: ext/vnua/veterinary_beapp/modules/material/service/impl/MaterialMovementQueryServiceImpl.java
package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.modules.material.model.MaterialMovement;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialMovementRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialMovementQuery;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialMovementQueryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MaterialMovementQueryServiceImpl implements MaterialMovementQueryService {

    private final MaterialMovementRepository movementRepository;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<MaterialMovement> search(CustomMaterialMovementQuery.MovementFilterParam param, PageRequest pageRequest) {
        Specification<MaterialMovement> spec = CustomMaterialMovementQuery.getFilterMovement(param);
        return movementRepository.findAll(spec, pageRequest);
    }
}
