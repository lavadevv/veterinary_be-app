package ext.vnua.veterinary_beapp.modules.pcost.service;

import ext.vnua.veterinary_beapp.modules.pcost.model.LaborRate;
import ext.vnua.veterinary_beapp.modules.pcost.repository.custom.CustomLaborRateQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface LaborRateService {
    LaborRate create(LaborRate r);
    LaborRate update(Long id, LaborRate r);
    void delete(Long id);
    LaborRate get(Long id);
    List<LaborRate> listActive();
    Page<LaborRate> search(CustomLaborRateQuery.LaborRateFilterParam p, PageRequest pageRequest);
}
