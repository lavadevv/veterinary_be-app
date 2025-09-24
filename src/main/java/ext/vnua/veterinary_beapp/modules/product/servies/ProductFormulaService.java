package ext.vnua.veterinary_beapp.modules.product.servies;

import ext.vnua.veterinary_beapp.modules.product.dto.ProductFormulaDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.UpsertFormulaRequest;

import java.util.List;

public interface ProductFormulaService {
    ProductFormulaDto upsertFormula(UpsertFormulaRequest request); // nếu trùng version -> update
    ProductFormulaDto getActiveFormula(Long productId);
    List<ProductFormulaDto> listFormulas(Long productId);
    void activateFormula(Long formulaId); // set active, đồng thời de-activate bản khác cùng product (nếu muốn)
    void deleteFormula(Long formulaId);
}