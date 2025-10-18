package ext.vnua.veterinary_beapp.modules.pricing.service;

import ext.vnua.veterinary_beapp.modules.pricing.dto.BrandPriceRowDto;
import ext.vnua.veterinary_beapp.modules.pricing.dto.PricingCalculateRequest;

import java.util.List;

public interface PricingService {
    List<BrandPriceRowDto> calculateBrandPrices(Long productId, PricingCalculateRequest request);
}

