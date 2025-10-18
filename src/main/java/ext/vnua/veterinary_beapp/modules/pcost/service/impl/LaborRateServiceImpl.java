package ext.vnua.veterinary_beapp.modules.pcost.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.pcost.model.LaborRate;
import ext.vnua.veterinary_beapp.modules.pcost.repository.LaborRateRepository;
import ext.vnua.veterinary_beapp.modules.pcost.repository.custom.CustomLaborRateQuery;
import ext.vnua.veterinary_beapp.modules.pcost.service.LaborRateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class LaborRateServiceImpl implements LaborRateService {

    private final LaborRateRepository repo;

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "LaborRate", description = "Tạo đơn giá nhân công")
    public LaborRate create(LaborRate r) {
        repo.findByCode(r.getCode()).ifPresent(x -> { throw new DataExistException("Mã nhân công đã tồn tại: " + r.getCode()); });
        return repo.saveAndFlush(r);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "LaborRate", description = "Cập nhật đơn giá nhân công")
    public LaborRate update(Long id, LaborRate r) {
        LaborRate e = repo.findById(id).orElseThrow(() -> new DataExistException("Đơn giá nhân công không tồn tại"));
        if (!e.getCode().equals(r.getCode())) {
            repo.findByCode(r.getCode()).ifPresent(x -> { throw new DataExistException("Mã nhân công đã tồn tại: " + r.getCode()); });
        }
        e.setCode(r.getCode());
        e.setName(r.getName());
        e.setUnit(r.getUnit());
        e.setPricePerUnit(r.getPricePerUnit());
        e.setEffectiveDate(r.getEffectiveDate());
        e.setIsActive(Boolean.TRUE.equals(r.getIsActive()));
        e.setNotes(r.getNotes());
        return repo.saveAndFlush(e);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "LaborRate", description = "Xoá đơn giá nhân công")
    public void delete(Long id) { repo.deleteById(id); }

    @Override
    public LaborRate get(Long id) {
        return repo.findById(id).orElseThrow(() -> new DataExistException("Đơn giá nhân công không tồn tại"));
    }

    @Override
    public java.util.List<LaborRate> listActive() {
        return repo.findByIsActiveTrueOrderByEffectiveDateDesc();
    }

    @Override
    public Page<LaborRate> search(CustomLaborRateQuery.LaborRateFilterParam p, PageRequest pageRequest) {
        Specification<LaborRate> spec = CustomLaborRateQuery.getFilter(p);
        return repo.findAll(spec, pageRequest);
    }
}
