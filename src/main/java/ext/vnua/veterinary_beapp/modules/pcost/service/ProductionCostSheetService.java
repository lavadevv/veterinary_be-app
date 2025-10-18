// ext/vnua/veterinary_beapp/modules/pcost/service/ProductionCostSheetService.java
package ext.vnua.veterinary_beapp.modules.pcost.service;

import ext.vnua.veterinary_beapp.modules.pcost.dto.GetProductionCostSheets;
import ext.vnua.veterinary_beapp.modules.pcost.dto.ProductionCostSheetDto;
import ext.vnua.veterinary_beapp.modules.pcost.dto.UpsertProductionCostSheetRequest;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ProductionCostSheetService {
    ProductionCostSheetDto create(UpsertProductionCostSheetRequest req);
    ProductionCostSheetDto update(Long id, UpsertProductionCostSheetRequest req);
    ProductionCostSheetDto get(Long id);
    ProductionCostSheetDto getByCode(String code);
    List<ProductionCostSheetDto> listByProduct(Long productId);
    void delete(Long id);

    /** Search paging: trả về map { total, items } để dễ gói BaseResponse */
    Page<ProductionCostSheetDto> search(GetProductionCostSheets req);}
