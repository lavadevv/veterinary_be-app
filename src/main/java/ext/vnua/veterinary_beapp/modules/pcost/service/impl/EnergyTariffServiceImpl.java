// ext/vnua/veterinary_beapp/modules/pcost/service/impl/EnergyTariffServiceImpl.java
package ext.vnua.veterinary_beapp.modules.pcost.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.modules.pcost.model.EnergyTariff;
import ext.vnua.veterinary_beapp.modules.pcost.repository.EnergyTariffRepository;
import ext.vnua.veterinary_beapp.modules.pcost.repository.custom.CustomEnergyTariffQuery;
import ext.vnua.veterinary_beapp.modules.pcost.service.EnergyTariffService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @RequiredArgsConstructor
public class EnergyTariffServiceImpl implements EnergyTariffService {

    private final EnergyTariffRepository repo;

    @Override
    public EnergyTariff create(EnergyTariff r) {
        if (repo.existsByCode(r.getCode())) {
            throw new DataExistException("Mã giá điện đã tồn tại: " + r.getCode());
        }
        return repo.saveAndFlush(r);
    }

    @Override
    public EnergyTariff update(Long id, EnergyTariff r) {
        EnergyTariff e = repo.findById(id).orElseThrow(() -> new DataExistException("Không tìm thấy giá điện"));
        if (!e.getCode().equals(r.getCode()) && repo.existsByCode(r.getCode())) {
            throw new DataExistException("Mã giá điện đã tồn tại: " + r.getCode());
        }
        e.setCode(r.getCode());
        e.setName(r.getName());
        e.setUnit(r.getUnit());
        e.setPricePerUnit(r.getPricePerUnit());
        e.setEffectiveDate(r.getEffectiveDate());
        e.setIsActive(r.getIsActive());
        e.setNotes(r.getNotes());
        return repo.saveAndFlush(e);
    }

    @Override
    public EnergyTariff get(Long id) { return repo.findById(id).orElseThrow(() -> new DataExistException("Không tìm thấy giá điện")); }

    @Override
    public void delete(Long id) { repo.deleteById(id); }

    @Override
    public List<EnergyTariff> listActive() { return repo.findAll((root, q, cb) -> cb.equal(root.get("isActive"), true)); }

    // NEW
    @Override
    public Page<EnergyTariff> search(CustomEnergyTariffQuery.EnergyTariffFilterParam param, PageRequest pageRequest) {
        return repo.findAll(CustomEnergyTariffQuery.getFilter(param), pageRequest);
    }
}
