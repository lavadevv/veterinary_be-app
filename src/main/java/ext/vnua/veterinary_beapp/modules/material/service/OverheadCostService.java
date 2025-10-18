package ext.vnua.veterinary_beapp.modules.material.service;

import ext.vnua.veterinary_beapp.modules.material.dto.request.cost.CreateOverheadCostRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.cost.UpdateOverheadCostRequest;
import ext.vnua.veterinary_beapp.modules.material.model.OverheadCost;

import java.time.LocalDate;
import java.util.List;

/** Quản lý Chi phí sản xuất chung (Overhead). */
public interface OverheadCostService {
    OverheadCost create(CreateOverheadCostRequest req);
    OverheadCost update(UpdateOverheadCostRequest req);
    void delete(Long id);
    OverheadCost get(Long id);
    List<OverheadCost> listByDateRange(LocalDate from, LocalDate to);
    List<OverheadCost> listByPeriod(LocalDate periodMonth); // periodMonth = bất kỳ ngày trong tháng => auto về mùng 1
}
