package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.dto.request.formulaPrice.UpdateFormulaPriceRequest;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialPriceHistory;

import java.util.List;

public interface FormulaPriceService {
    void updateFormulaPrice(UpdateFormulaPriceRequest req);
    List<MaterialPriceHistory> getPriceHistory(Long materialId);
}
