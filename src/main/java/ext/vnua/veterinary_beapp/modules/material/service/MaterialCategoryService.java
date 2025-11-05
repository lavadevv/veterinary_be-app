// File: ext/vnua/veterinary_beapp/modules/material/service/MaterialCategoryService.java
package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.model.MaterialCategory;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialCategoryQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface MaterialCategoryService {

    Page<MaterialCategory> search(CustomMaterialCategoryQuery.MaterialCategoryQuery.CategoryFilterParam param,
                                  PageRequest pageRequest);

    List<MaterialCategory> listAll(); // sắp xếp A→Z

    MaterialCategory getById(Long id);

    MaterialCategory create(String categoryName);

    MaterialCategory update(Long id, String newName);

    void delete(Long id);

    List<MaterialCategory> deleteBulk(List<Long> ids);
}
