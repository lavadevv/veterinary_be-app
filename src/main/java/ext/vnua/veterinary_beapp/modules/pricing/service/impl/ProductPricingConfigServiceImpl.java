package ext.vnua.veterinary_beapp.modules.pricing.service.impl;

import ext.vnua.veterinary_beapp.modules.pricing.dto.PricingLinesRawDto;
import ext.vnua.veterinary_beapp.modules.pricing.dto.PricingLinesSaveRequest;
import ext.vnua.veterinary_beapp.modules.pricing.model.ProductPricingConfig;
import ext.vnua.veterinary_beapp.modules.pricing.model.ProductPricingLine;
import ext.vnua.veterinary_beapp.modules.pricing.repository.ProductPricingConfigRepository;
import ext.vnua.veterinary_beapp.modules.pricing.repository.ProductPricingLineRepository;
import ext.vnua.veterinary_beapp.modules.pricing.service.ProductPricingConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPricingConfigServiceImpl implements ProductPricingConfigService {

    private final ProductPricingConfigRepository configRepo;
    private final ProductPricingLineRepository lineRepo;

    @Override
    @Transactional
    public PricingLinesRawDto saveAll(Long productId, PricingLinesSaveRequest req) {
        // 1) Upsert header
        ProductPricingConfig cfg = configRepo.findById(productId).orElseGet(ProductPricingConfig::new);
        cfg.setProductId(productId);
        cfg.setFormulaId(req.getFormulaId());
        cfg.setBatchSizeKg(req.getBatchSizeKg());
        cfg.setUpdatedAt(LocalDateTime.now());
        configRepo.save(cfg);

        // 2) Replace lines (mặc định)
        if (req.getReplace() == null || Boolean.TRUE.equals(req.getReplace())) {
            lineRepo.deleteByProductId(productId);
        }

        // 3) Insert batch
        LocalDateTime now = LocalDateTime.now();
        for (PricingLinesSaveRequest.Line l : req.getLines()) {
            ProductPricingLine e = new ProductPricingLine();
            e.setProductId(productId);
            e.setStt(l.getStt());
            e.setManualCode(l.getManualCode());
            e.setSheetCode(l.getSheetCode());
            e.setPackSize(l.getPackSize());
            e.setSpecName(l.getSpecName());
            e.setProfitPercent(l.getProfitPercent());
            e.setIsActive(l.getIsActive() == null ? true : l.getIsActive());
            e.setCreatedAt(now);
            e.setUpdatedAt(now);
            lineRepo.save(e);
        }

        return getRawByProduct(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public PricingLinesRawDto getRawByProduct(Long productId) {
        ProductPricingConfig cfg = configRepo.findById(productId)
                .orElseGet(() -> {
                    ProductPricingConfig c = new ProductPricingConfig();
                    c.setProductId(productId);
                    c.setFormulaId(null);
                    c.setBatchSizeKg(null);
                    c.setUpdatedAt(null);
                    return c;
                });

        List<ProductPricingLine> lines = lineRepo.findByProductIdOrderBySttAscIdAsc(productId);

        return new PricingLinesRawDto(
                cfg.getProductId(),
                cfg.getFormulaId(),
                cfg.getBatchSizeKg(),
                cfg.getUpdatedAt(),
                lines.stream().sorted(Comparator.comparing(ProductPricingLine::getStt).thenComparing(ProductPricingLine::getId))
                        .map(e -> PricingLinesRawDto.Line.builder()
                                .id(e.getId())
                                .stt(e.getStt())
                                .manualCode(e.getManualCode())
                                .sheetCode(e.getSheetCode())
                                .packSize(e.getPackSize())
                                .specName(e.getSpecName())
                                .profitPercent(e.getProfitPercent())
                                .isActive(e.getIsActive())
                                .createdAt(e.getCreatedAt())
                                .updatedAt(e.getUpdatedAt())
                                .build()
                        ).toList()
        );
    }
}
