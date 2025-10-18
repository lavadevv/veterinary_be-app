package ext.vnua.veterinary_beapp.modules.product.services;

import ext.vnua.veterinary_beapp.modules.product.dto.ProductFormulaDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.formula.ProductFormulaListRow;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.UpsertFormulaRequest;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormula;
import ext.vnua.veterinary_beapp.modules.product.repository.custom.CustomProductFormulaQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ProductFormulaService {
    ProductFormulaDto upsertFormula(UpsertFormulaRequest request); // nếu trùng version -> update
    ProductFormulaDto getActiveFormula(Long productId);
    List<ProductFormulaDto> listFormulas(Long productId);
    void activateFormula(Long formulaId); // set active, đồng thời de-activate bản khác cùng product (nếu muốn)
    void deleteFormula(Long formulaId);
    /** Lấy trang công thức (entity) theo filter Spec – để map ra ListRow */
    Page<ProductFormula> getAllFormulas(CustomProductFormulaQuery.ProductFormulaFilterParam param, PageRequest pageRequest);

    /** Trả sẵn ListRow để FE render nhanh */
    Page<ProductFormulaListRow> getAllFormulaRows(CustomProductFormulaQuery.ProductFormulaFilterParam param, PageRequest pageRequest);

    /** Lấy chi tiết theo ID cho màn list toàn cục click vào */
    ProductFormulaDto getById(Long formulaId);
}