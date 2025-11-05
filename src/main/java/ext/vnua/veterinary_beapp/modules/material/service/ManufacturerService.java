package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.ManufacturerDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.manufacturer.CreateManufacturerRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.manufacturer.UpdateManufacturerRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Manufacturer;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomManufacturerQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ManufacturerService {
    Page<Manufacturer> getAllManufacturer(CustomManufacturerQuery.ManuFilterParam param, PageRequest pageRequest);

    ManufacturerDto selectById(Long id);

    ManufacturerDto selectByCode(String manufacturerCode);

    List<ManufacturerDto> selectActive();

    ManufacturerDto create(CreateManufacturerRequest req);

    ManufacturerDto update(UpdateManufacturerRequest req);

    void toggleActive(Long id);

    void delete(Long id);

    List<ManufacturerDto> deleteAllByIds(List<Long> ids);
}
