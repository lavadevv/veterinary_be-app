package ext.vnua.veterinary_beapp.modules.pricing.controller;

import ext.vnua.veterinary_beapp.modules.pricing.dto.BrandPriceRowDto;
import ext.vnua.veterinary_beapp.modules.pricing.dto.PricingCalculateRequest;
import ext.vnua.veterinary_beapp.modules.pricing.dto.PricingLinesRawDto;
import ext.vnua.veterinary_beapp.modules.pricing.dto.PricingLinesSaveRequest;
import ext.vnua.veterinary_beapp.modules.pricing.service.PricingService;
import ext.vnua.veterinary_beapp.modules.pricing.service.ProductPricingConfigService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    private final ProductPricingConfigService service;

    /**
     * Tính bảng giá theo thương hiệu cho 1 sản phẩm.
     * Input: formulaId, batchSizeKg, danh sách brands (sheetCode, packSize, specName, manualCode, profitPercent)
     * Output: danh sách hàng (STT, Mã chi phí, Kích cỡ đóng gói, Tên quy cách, CPNL, CPSX, Cost, %LN, Giá bán)
     */
    @PostMapping("/product/{productId}/brands/calc")
    @ApiOperation("Tính giá theo thương hiệu của 1 sản phẩm")
    public ResponseEntity<List<BrandPriceRowDto>> calcBrandPrices(
            @PathVariable Long productId,
            @Valid @RequestBody PricingCalculateRequest request
    ) {
        return ResponseEntity.ok(pricingService.calculateBrandPrices(productId, request));
    }

    /**
     * Ghi đè toàn bộ cấu hình giá của 1 product (save raw).
     */
    @PutMapping("/product/{productId}/lines/save")
    @ApiOperation("Ghi đè cấu hình giá (raw) theo sản phẩm")
    public ResponseEntity<PricingLinesRawDto> saveLines(
            @PathVariable Long productId,
            @Valid @RequestBody PricingLinesSaveRequest request
    ) {
        return ResponseEntity.ok(service.saveAll(productId, request));
    }

    /**
     * Xem dữ liệu đã lưu (raw) cho 1 product (để người dùng xem lại).
     */
    @GetMapping("/product/{productId}/lines/raw")
    @ApiOperation("Lấy cấu hình giá (raw) đã lưu theo sản phẩm")
    public ResponseEntity<PricingLinesRawDto> getRaw(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(service.getRawByProduct(productId));
    }

    @GetMapping("/product/{productId}/brands")
    @ApiOperation("Tính và trả về bảng giá dựa trên cấu hình đã lưu của sản phẩm")
    public ResponseEntity<List<BrandPriceRowDto>> getCalculatedFromSaved(@PathVariable Long productId) {
        PricingLinesRawDto raw = service.getRawByProduct(productId);

        // Nếu chưa lưu gì: trả rỗng cho FE hiển thị trạng thái “chưa cấu hình”
        if (raw.getFormulaId() == null || raw.getBatchSizeKg() == null) {
            return ResponseEntity.ok(List.of());
        }
        if (raw.getLines() == null || raw.getLines().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        // Map RAW -> request tính toán
        var req = new PricingCalculateRequest();
        req.setFormulaId(raw.getFormulaId());
        req.setBatchSizeKg(raw.getBatchSizeKg());

        var brandInputs = raw.getLines().stream()
                .filter(PricingLinesRawDto.Line::getIsActive)
                .sorted(Comparator.comparing(PricingLinesRawDto.Line::getStt)
                        .thenComparing(PricingLinesRawDto.Line::getId))
                .map(l -> {
                    var bi = new PricingCalculateRequest.BrandInput();
                    bi.setManualCode(l.getManualCode());
                    bi.setSheetCode(l.getSheetCode());
                    bi.setPackSize(l.getPackSize());
                    bi.setSpecName(l.getSpecName());
                    bi.setProfitPercent(l.getProfitPercent());
                    return bi;
                })
                .toList();
        req.setBrands(brandInputs);

        // Nếu sau khi lọc không còn dòng active => trả rỗng
        if (req.getBrands().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(pricingService.calculateBrandPrices(productId, req));
    }
}
