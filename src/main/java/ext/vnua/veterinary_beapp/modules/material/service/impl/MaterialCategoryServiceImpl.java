// File: ext/vnua/veterinary_beapp/modules/material/service/impl/MaterialCategoryServiceImpl.java
package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialCategory;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialCategoryRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialCategoryQuery;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialCategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialCategoryServiceImpl implements MaterialCategoryService {

    private final MaterialCategoryRepository repository;

    @Override
    public Page<MaterialCategory> search(CustomMaterialCategoryQuery.MaterialCategoryQuery.CategoryFilterParam param,
                                         PageRequest pageRequest) {
        Specification<MaterialCategory> spec = CustomMaterialCategoryQuery.getFilter(param);
        return repository.findAll(spec, pageRequest);
    }

    @Override
    public List<MaterialCategory> listAll() {
        return repository.findAllByOrderByCategoryNameAsc();
    }

    @Override
    public MaterialCategory getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new DataExistException("MaterialCategory không tồn tại"));
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "MaterialCategory", description = "Tạo mới loại vật liệu")
    public MaterialCategory create(String categoryName) {
        String name = safe(categoryName);
        if (name.isEmpty()) throw new MyCustomException("Tên loại không được để trống");

        repository.findByCategoryName(name)
                .ifPresent(x -> { throw new DataExistException("Tên loại đã tồn tại"); });

        MaterialCategory mc = new MaterialCategory();
        mc.setCategoryName(name);
        return repository.saveAndFlush(mc);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "MaterialCategory", description = "Cập nhật loại vật liệu")
    public MaterialCategory update(Long id, String newName) {
        MaterialCategory existing = repository.findById(id)
                .orElseThrow(() -> new DataExistException("MaterialCategory không tồn tại"));

        String name = safe(newName);
        if (name.isEmpty()) throw new MyCustomException("Tên loại không được để trống");

        repository.findByCategoryNameAndIdNot(name, id)
                .ifPresent(x -> { throw new DataExistException("Tên loại đã tồn tại"); });

        existing.setCategoryName(name);
        return repository.saveAndFlush(existing);
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "MaterialCategory", description = "Xoá loại vật liệu")
    public void delete(Long id) {
        MaterialCategory existing = repository.findById(id)
                .orElseThrow(() -> new DataExistException("MaterialCategory không tồn tại"));

        try {
            repository.delete(existing);
        } catch (DataIntegrityViolationException ex) {
            throw new MyCustomException("Không thể xoá: đang được tham chiếu bởi materials");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "MaterialCategory", description = "Xoá danh sách loại vật liệu")
    public List<MaterialCategory> deleteBulk(List<Long> ids) {
        List<MaterialCategory> result = new ArrayList<>();
        for (Long id : ids) {
            MaterialCategory existing = repository.findById(id)
                    .orElseThrow(() -> new MyCustomException("MaterialCategory không tồn tại: " + id));
            try {
                result.add(existing);
                repository.delete(existing);
            } catch (DataIntegrityViolationException ex) {
                throw new MyCustomException("Không thể xoá loại đang được tham chiếu: " + existing.getCategoryName());
            }
        }
        return result;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
