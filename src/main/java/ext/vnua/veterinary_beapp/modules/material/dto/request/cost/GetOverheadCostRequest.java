package ext.vnua.veterinary_beapp.modules.material.dto.request.cost;

import ext.vnua.veterinary_beapp.modules.material.enums.OverheadType;

public record GetOverheadCostRequest(
        Integer year, Integer month, OverheadType type, String keyword,
        int start, int limit
) {}