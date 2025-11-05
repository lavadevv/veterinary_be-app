package ext.vnua.veterinary_beapp.modules.pcost.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.model.UnitOfMeasure;
import ext.vnua.veterinary_beapp.modules.material.repository.UnitOfMeasureRepository;
import ext.vnua.veterinary_beapp.modules.pcost.model.LaborRate;
import ext.vnua.veterinary_beapp.modules.pcost.repository.LaborRateRepository;
import ext.vnua.veterinary_beapp.modules.pcost.repository.custom.CustomLaborRateQuery;
import ext.vnua.veterinary_beapp.modules.pcost.service.LaborRateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @RequiredArgsConstructor
public class LaborRateServiceImpl implements LaborRateService {

    private final LaborRateRepository repo;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "LaborRate", description = "Tạo đơn giá nhân công")
    public LaborRate create(LaborRate r) {
        // Normalize code to lowercase
        String normalizedCode = r.getCode() != null ? r.getCode().trim().toLowerCase() : null;
        if (normalizedCode == null || normalizedCode.isEmpty()) {
            throw new MyCustomException("Mã biểu giá không được để trống");
        }
        r.setCode(normalizedCode);
        
        // Check code uniqueness
        repo.findByCode(normalizedCode).ifPresent(x -> { 
            throw new DataExistException("Mã nhân công đã tồn tại: " + normalizedCode); 
        });
        
        // Validate and set UnitOfMeasure if provided
        if (r.getUnitOfMeasure() != null && r.getUnitOfMeasure().getId() != null) {
            UnitOfMeasure uom = unitOfMeasureRepository.findById(r.getUnitOfMeasure().getId())
                    .orElseThrow(() -> new MyCustomException("Đơn vị tính không tồn tại"));
            r.setUnitOfMeasure(uom);
        }
        
        return repo.saveAndFlush(r);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "LaborRate", description = "Cập nhật đơn giá nhân công")
    public LaborRate update(Long id, LaborRate r) {
        LaborRate e = repo.findById(id).orElseThrow(() -> new DataExistException("Đơn giá nhân công không tồn tại"));
        
        // Normalize code to lowercase
        String normalizedCode = r.getCode() != null ? r.getCode().trim().toLowerCase() : null;
        if (normalizedCode == null || normalizedCode.isEmpty()) {
            throw new MyCustomException("Mã biểu giá không được để trống");
        }
        
        // Check code uniqueness if changed
        if (!e.getCode().equals(normalizedCode)) {
            repo.findByCode(normalizedCode).ifPresent(x -> { 
                throw new DataExistException("Mã nhân công đã tồn tại: " + normalizedCode); 
            });
        }
        
        e.setCode(normalizedCode);
        e.setName(r.getName());
        
        // Validate and set UnitOfMeasure if provided
        if (r.getUnitOfMeasure() != null && r.getUnitOfMeasure().getId() != null) {
            UnitOfMeasure uom = unitOfMeasureRepository.findById(r.getUnitOfMeasure().getId())
                    .orElseThrow(() -> new MyCustomException("Đơn vị tính không tồn tại"));
            e.setUnitOfMeasure(uom);
        }
        
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
    @Transactional
    public LaborRate get(Long id) {
        LaborRate rate = repo.findById(id).orElseThrow(() -> new DataExistException("Đơn giá nhân công không tồn tại"));
        // Đảm bảo unitOfMeasure được load (EntityGraph đã xử lý, nhưng safety check)
        if (rate.getUnitOfMeasure() != null && !Hibernate.isInitialized(rate.getUnitOfMeasure())) {
            Hibernate.initialize(rate.getUnitOfMeasure());
        }
        return rate;
    }

    @Override
    @Transactional
    public java.util.List<LaborRate> listActive() {
        // Repository đã dùng @EntityGraph để eager load unitOfMeasure
        List<LaborRate> rates = repo.findByIsActiveTrueOrderByEffectiveDateDesc();
        
        // Safety check: đảm bảo tất cả unitOfMeasure được initialize
        rates.forEach(rate -> {
            if (rate.getUnitOfMeasure() != null && !Hibernate.isInitialized(rate.getUnitOfMeasure())) {
                Hibernate.initialize(rate.getUnitOfMeasure());
            }
        });
        
        return rates;
    }

    @Override
    @Transactional
    public Page<LaborRate> search(CustomLaborRateQuery.LaborRateFilterParam p, PageRequest pageRequest) {
        Specification<LaborRate> spec = CustomLaborRateQuery.getFilter(p);
        Page<LaborRate> page = repo.findAll(spec, pageRequest);
        
        // Đảm bảo tất cả unitOfMeasure được initialize trong transaction
        page.getContent().forEach(rate -> {
            if (rate.getUnitOfMeasure() != null && !Hibernate.isInitialized(rate.getUnitOfMeasure())) {
                Hibernate.initialize(rate.getUnitOfMeasure());
            }
        });
        
        return page;
    }
}
