package ext.vnua.veterinary_beapp.modules.reports.services;

import ext.vnua.veterinary_beapp.modules.reports.dto.MaterialCostVarianceRequest;
import ext.vnua.veterinary_beapp.modules.reports.dto.MaterialCostVarianceResponse;

public interface MaterialCostVarianceReportService {
    MaterialCostVarianceResponse buildReport(MaterialCostVarianceRequest req, boolean includeOverheads);
}
