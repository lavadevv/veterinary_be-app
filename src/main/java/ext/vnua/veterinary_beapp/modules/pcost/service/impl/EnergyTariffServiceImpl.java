// ext/vnua/veterinary_beapp/modules/pcost/service/impl/EnergyTariffServiceImpl.java
package ext.vnua.veterinary_beapp.modules.pcost.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.material.model.UnitOfMeasure;
import ext.vnua.veterinary_beapp.modules.material.repository.UnitOfMeasureRepository;
import ext.vnua.veterinary_beapp.modules.pcost.model.EnergyTariff;
import ext.vnua.veterinary_beapp.modules.pcost.repository.EnergyTariffRepository;
import ext.vnua.veterinary_beapp.modules.pcost.repository.custom.CustomEnergyTariffQuery;
import ext.vnua.veterinary_beapp.modules.pcost.service.EnergyTariffService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @RequiredArgsConstructor
public class EnergyTariffServiceImpl implements EnergyTariffService {

    private final EnergyTariffRepository repo;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    @Override
    @Transactional
    public EnergyTariff create(EnergyTariff r) {
        // Normalize code to lowercase
        String normalizedCode = r.getCode() != null ? r.getCode().trim().toLowerCase() : null;
        if (normalizedCode == null || normalizedCode.isEmpty()) {
            throw new MyCustomException("Mã biểu giá không được để trống");
        }
        r.setCode(normalizedCode);
        
        // Check code uniqueness
        if (repo.existsByCode(normalizedCode)) {
            throw new DataExistException("Mã giá điện đã tồn tại: " + normalizedCode);
        }
        
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
    public EnergyTariff update(Long id, EnergyTariff r) {
        EnergyTariff e = repo.findById(id).orElseThrow(() -> new DataExistException("Không tìm thấy giá điện"));
        
        // Normalize code to lowercase
        String normalizedCode = r.getCode() != null ? r.getCode().trim().toLowerCase() : null;
        if (normalizedCode == null || normalizedCode.isEmpty()) {
            throw new MyCustomException("Mã biểu giá không được để trống");
        }
        
        // Check code uniqueness if changed
        if (!e.getCode().equals(normalizedCode) && repo.existsByCode(normalizedCode)) {
            throw new DataExistException("Mã giá điện đã tồn tại: " + normalizedCode);
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
        e.setIsActive(r.getIsActive());
        e.setNotes(r.getNotes());
        return repo.saveAndFlush(e);
    }

    @Override
    @Transactional
    public EnergyTariff get(Long id) {
        EnergyTariff tariff = repo.findById(id).orElseThrow(() -> new DataExistException("Không tìm thấy giá điện"));
        // Ensure unitOfMeasure is initialized
        if (tariff.getUnitOfMeasure() != null && !Hibernate.isInitialized(tariff.getUnitOfMeasure())) {
            Hibernate.initialize(tariff.getUnitOfMeasure());
        }
        return tariff;
    }

    @Override
    @Transactional
    public void delete(Long id) { 
        repo.deleteById(id); 
    }

    @Override
    @Transactional
    public List<EnergyTariff> listActive() {
        List<EnergyTariff> tariffs = repo.findByIsActiveTrueOrderByEffectiveDateDesc();
        
        // Safety check: ensure all unitOfMeasure are initialized
        tariffs.forEach(tariff -> {
            if (tariff.getUnitOfMeasure() != null && !Hibernate.isInitialized(tariff.getUnitOfMeasure())) {
                Hibernate.initialize(tariff.getUnitOfMeasure());
            }
        });
        
        return tariffs;
    }

    @Override
    @Transactional
    public Page<EnergyTariff> search(CustomEnergyTariffQuery.EnergyTariffFilterParam param, PageRequest pageRequest) {
        Page<EnergyTariff> page = repo.findAll(CustomEnergyTariffQuery.getFilter(param), pageRequest);
        
        // Ensure all unitOfMeasure are initialized within transaction
        page.getContent().forEach(tariff -> {
            if (tariff.getUnitOfMeasure() != null && !Hibernate.isInitialized(tariff.getUnitOfMeasure())) {
                Hibernate.initialize(tariff.getUnitOfMeasure());
            }
        });
        
        return page;
    }
}
