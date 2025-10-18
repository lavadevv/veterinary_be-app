// ext/vnua/veterinary_beapp/modules/material/service/impl/FormulaPriceServiceImpl.java
package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.dto.request.formulaPrice.UpdateFormulaPriceRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialPriceHistory;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialPriceHistoryRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialRepository;
import ext.vnua.veterinary_beapp.modules.material.service.FormulaPriceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service @RequiredArgsConstructor
public class FormulaPriceServiceImpl implements FormulaPriceService {

    private final MaterialRepository materialRepo;
    private final MaterialPriceHistoryRepository historyRepo;

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "Material", description = "Cập nhật giá công thức & ghi lịch sử")
    public void updateFormulaPrice(UpdateFormulaPriceRequest req) {
        Material m = materialRepo.findById(req.getMaterialId())
                .orElseThrow(() -> new DataExistException("Nguyên liệu không tồn tại"));

        // 1) lưu lịch sử
        MaterialPriceHistory h = new MaterialPriceHistory();
        h.setMaterial(m);
        h.setPrice(req.getNewPrice());
        h.setUom(m.getUnitOfMeasure());
        h.setEffectiveDate(LocalDate.now());
        h.setNote(req.getNote());
        historyRepo.save(h);

        // 2) cập nhật giá hiện hành trên Material (giá công thức)
        m.setFixedPrice(req.getNewPrice());
        materialRepo.save(m);
    }

    @Override
    public List<MaterialPriceHistory> getPriceHistory(Long materialId) {
        return historyRepo.findByMaterialIdOrderByEffectiveDateDesc(materialId);
    }
}
