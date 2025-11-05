// File: ext/vnua/veterinary_beapp/modules/material/service/MaterialFormTypeService.java
package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.dto.request.materialFormType.CreateMaterialFormTypeRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialFormType.UpdateMaterialFormTypeRequest;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialFormType;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialFormTypeQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface MaterialFormTypeService {

    Page<MaterialFormType> search(CustomMaterialFormTypeQuery.FilterParam param, PageRequest pageRequest);

    MaterialFormType create(CreateMaterialFormTypeRequest req);

    MaterialFormType update(UpdateMaterialFormTypeRequest req);

    void delete(Long id);
}
