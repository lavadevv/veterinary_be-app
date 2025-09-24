package ext.vnua.veterinary_beapp.modules.production.services;

import ext.vnua.veterinary_beapp.modules.production.dto.ProductionLineDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionLine.CreateProductionLineRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionLine.UpdateProductionLineRequest;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionLine;
import ext.vnua.veterinary_beapp.modules.production.repository.custom.CustomProductionLineQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ProductionLineService {

    Page<ProductionLine> getAllLines(CustomProductionLineQuery.ProductionLineFilterParam param, PageRequest pr);

    ProductionLineDto getById(Long id);

    ProductionLineDto getByCode(String lineCode);

    ProductionLineDto create(CreateProductionLineRequest req);

    ProductionLineDto update(UpdateProductionLineRequest req);

    void toggleStatus(Long id);

    void delete(Long id);

    List<ProductionLineDto> deleteAll(List<Long> ids);

    // Additional utility methods
    boolean existsByCode(String lineCode);

    List<ProductionLineDto> findActiveLines();

    List<ProductionLineDto> findByStatus(String status);

    long countByStatus(String status);

    List<ProductionLineDto> searchByName(String name);
}