// File: ext/vnua/veterinary_beapp/modules/material/service/impl/MaterialFormTypeServiceImpl.java
package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialFormType.CreateMaterialFormTypeRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialFormType.UpdateMaterialFormTypeRequest;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialFormType;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialFormTypeRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialFormTypeQuery;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialFormTypeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MaterialFormTypeServiceImpl implements MaterialFormTypeService {

    private final MaterialFormTypeRepository repo;

    @Override
    public Page<MaterialFormType> search(CustomMaterialFormTypeQuery.FilterParam param, PageRequest pageRequest) {
        Specification<MaterialFormType> spec = CustomMaterialFormTypeQuery.getFilter(param);
        return repo.findAll(spec, pageRequest);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "MaterialFormType", description = "Tạo mới dạng vật liệu")
    public MaterialFormType create(CreateMaterialFormTypeRequest req) {
        repo.findByName(req.getName().trim())
                .ifPresent(x -> { throw new DataExistException("Tên dạng vật liệu đã tồn tại"); });
        try {
            MaterialFormType e = new MaterialFormType();
            e.setName(req.getName().trim());
            return repo.saveAndFlush(e);
        } catch (Exception ex) {
            throw new MyCustomException("Không thể tạo dạng vật liệu");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "MaterialFormType", description = "Cập nhật dạng vật liệu")
    public MaterialFormType update(UpdateMaterialFormTypeRequest req) {
        MaterialFormType cur = repo.findById(req.getId())
                .orElseThrow(() -> new DataExistException("Dạng vật liệu không tồn tại"));

        // check trùng tên
        repo.findByNameAndIdNot(req.getName().trim(), req.getId())
                .ifPresent(x -> { throw new DataExistException("Tên dạng vật liệu đã tồn tại"); });

        try {
            cur.setName(req.getName().trim());
            return repo.saveAndFlush(cur);
        } catch (Exception ex) {
            throw new MyCustomException("Không thể cập nhật dạng vật liệu");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "MaterialFormType", description = "Xoá dạng vật liệu")
    public void delete(Long id) {
        MaterialFormType cur = repo.findById(id)
                .orElseThrow(() -> new DataExistException("Dạng vật liệu không tồn tại"));
        try {
            repo.delete(cur);
        } catch (DataIntegrityViolationException ex) {
            throw new MyCustomException("Không thể xoá: có thể đang được tham chiếu bởi materials");
        } catch (Exception ex) {
            throw new MyCustomException("Không thể xoá dạng vật liệu");
        }
    }
}
