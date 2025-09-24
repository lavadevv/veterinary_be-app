package ext.vnua.veterinary_beapp.modules.production.services;

import ext.vnua.veterinary_beapp.modules.production.dto.ProductionBatchRecordDto;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionBatchRecord.CreateProductionBatchRecordRequest;
import ext.vnua.veterinary_beapp.modules.production.dto.request.productionBatchRecord.UpdateProductionBatchRecordRequest;
import ext.vnua.veterinary_beapp.modules.production.repository.custom.CustomProductionBatchRecordQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ProductionBatchRecordService {
    Page<ProductionBatchRecordDto> getAll(CustomProductionBatchRecordQuery.ProductionBatchRecordFilterParam param, PageRequest pr);
    ProductionBatchRecordDto getById(Long id);
    ProductionBatchRecordDto create(CreateProductionBatchRecordRequest req);
    ProductionBatchRecordDto update(UpdateProductionBatchRecordRequest req);
    void delete(Long id);
    List<ProductionBatchRecordDto> deleteAll(List<Long> ids);
}
