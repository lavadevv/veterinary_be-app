package ext.vnua.veterinary_beapp.modules.pricing.service;

import ext.vnua.veterinary_beapp.modules.pricing.dto.PricingLinesRawDto;
import ext.vnua.veterinary_beapp.modules.pricing.dto.PricingLinesSaveRequest;

public interface ProductPricingConfigService {

    /**
     * Ghi đè (hoặc upsert) toàn bộ cấu hình giá của 1 sản phẩm.
     */
    PricingLinesRawDto saveAll(Long productId, PricingLinesSaveRequest req);

    /**
     * Lấy dữ liệu đã lưu (raw) theo product để người dùng xem/chỉnh.
     */
    PricingLinesRawDto getRawByProduct(Long productId);
}
