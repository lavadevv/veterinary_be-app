// File: src/main/java/ext/vnua/veterinary_beapp/modules/product/controller/ProductFormulaController.java
package ext.vnua.veterinary_beapp.modules.product.controller;

import ext.vnua.veterinary_beapp.dto.response.BaseResponse;
import ext.vnua.veterinary_beapp.modules.product.dto.ProductFormulaDto;
import ext.vnua.veterinary_beapp.modules.product.dto.request.formula.GetProductFormulaRequest;
import ext.vnua.veterinary_beapp.modules.product.dto.request.formula.ProductFormulaListRow;
import ext.vnua.veterinary_beapp.modules.product.dto.request.productBatch.UpsertFormulaRequest;
import ext.vnua.veterinary_beapp.modules.product.mapper.ProductFormulaMapper;
import ext.vnua.veterinary_beapp.modules.product.model.FormulaHeader;
import ext.vnua.veterinary_beapp.modules.product.repository.FormulaHeaderRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductFormulaRepository;
import ext.vnua.veterinary_beapp.modules.product.services.ProductFormulaService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * API Công thức sản phẩm (Phiên bản hoá theo Header/Version)
 *
 * QUY ƯỚC HIỂN THỊ THEO YÊU CẦU KH:
 * - "Danh mục công thức": Mỗi công thức (Header = công thức logic) chỉ hiển thị phiên bản MỚI NHẤT.
 * - Lịch sử: Khi vào xem/ tìm kiếm chi tiết thì thấy ĐỦ các phiên bản (version) theo ngày tạo/sửa.
 * - Khi tạo bản mới: luôn ghi nhận 1 VERSION mới (không overwrite).
 *
 * GLOSSARY:
 * - Header (FormulaHeader): định danh công thức (formulaCode, formulaName, mô tả, gắn nhiều sản phẩm dùng chung).
 * - Version (ProductFormula): một lần thiết lập/ chỉnh sửa → tạo 1 bản version mới, có isActive trong phạm vi Header.
 */
@RestController
@RequestMapping("/product-formula")
@RequiredArgsConstructor
public class ProductFormulaController {

    private final ProductFormulaService formulaService;
    private final FormulaHeaderRepository headerRepo;
    private final ProductFormulaRepository formulaRepository;
    private final ProductFormulaMapper formulaMapper;

    /**
     * Tạo phiên bản công thức mới cho 1 Header (theo formulaCode).
     * - Body BẮT BUỘC có: formulaCode
     * - version: optional (server sẽ auto yyyyMMdd-HHmmss nếu không truyền)
     * - productIds: optional (gắn Header ↔ nhiều sản phẩm dùng chung)
     * - items[]: danh sách NVL (theo % hoặc theo quantity/unit)
     *
     * LƯU Ý:
     * - Mỗi lần gọi là tạo 1 VERSION MỚI (không cập nhật đè bản cũ).
     * - Nếu isActive=true → hệ thống sẽ hạ isActive=false ở các version khác cùng Header.
     */
    @ApiOperation(value = "Tạo phiên bản công thức mới (Header/Version). Body cần formulaCode; version optional")
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','QC_STAFF')")
    public ResponseEntity<?> upsert(@Valid @RequestBody UpsertFormulaRequest req) {
        ProductFormulaDto dto = formulaService.upsertFormula(req);
        return ResponseEntity.ok(dto);
    }

    /**
     * Đặt 1 phiên bản công thức là Active (độc nhất trong cùng Header).
     * - Tự động set các version khác (cùng Header) về isActive=false
     */
    @ApiOperation(value = "Kích hoạt công thức (độc nhất trong cùng Header)")
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER')")
    public ResponseEntity<?> activate(@ApiParam(value = "ID công thức", required = true) @PathVariable("id") Long id) {
        formulaService.activateFormula(id);
        return ResponseEntity.ok("Đã kích hoạt công thức");
    }

    /**
     * Lấy công thức Active áp dụng cho 1 sản phẩm.
     * - Logic: tìm các Header có gắn productId → lấy version đang isActive mới nhất.
     */
    @ApiOperation(value = "Xem công thức active của sản phẩm")
    @GetMapping("/product/{productId}/active")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','QC_STAFF','VIEWER')")
    public ResponseEntity<?> getActive(@PathVariable Long productId) {
        return ResponseEntity.ok(formulaService.getActiveFormula(productId));
    }

    /**
     * Danh sách công thức theo sản phẩm:
     * - TRẢ VỀ: Mỗi Header chỉ trả bản version MỚI NHẤT (đúng yêu cầu 'danh mục' dưới góc nhìn 1 product).
     * - Dùng cho màn chi tiết sản phẩm muốn xem công thức hiện hành theo từng Header gắn với product.
     */
    @ApiOperation(value = "Danh sách công thức theo sản phẩm (mỗi Header trả version mới nhất)")
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','QC_STAFF','VIEWER')")
    public ResponseEntity<?> list(@PathVariable Long productId) {
        List<ProductFormulaDto> list = formulaService.listFormulas(productId);
        return BaseResponse.successListData(list, list.size());
    }

    /** Xoá 1 phiên bản công thức (không xoá Header). */
    @ApiOperation(value = "Xóa công thức (xóa 1 phiên bản)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        formulaService.deleteFormula(id);
        return ResponseEntity.ok("Đã xóa công thức");
    }

    /**
     * Danh sách toàn cục (filter + paging)
     * - Dùng cho trang tìm kiếm nâng cao, xem đủ bản ghi (không chỉ latest).
     * - Tham số filter qua query: productId / productCode / productName / version / active /
     *   fromCreatedDate / toCreatedDate / keywords / productCategory / formulationType / sortField / sortType
     */
    @ApiOperation(value = "Lấy danh sách công thức (toàn cục) – filter + paging (KH xem/tìm kiếm tất cả bản ghi)")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','QC_STAFF','VIEWER')")
    public ResponseEntity<?> listAll(@Valid @ModelAttribute GetProductFormulaRequest req) {
        Page<ProductFormulaListRow> page =
                formulaService.getAllFormulaRows(req, PageRequest.of(req.getStart(), req.getLimit()));
        return BaseResponse.successListData(page.getContent(), (int) page.getTotalElements());
    }

    /**
     * Xem chi tiết 1 phiên bản theo ID (phục vụ click từ list toàn cục).
     */
    @ApiOperation(value = "Xem chi tiết công thức theo ID (cho màn danh sách toàn cục)")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','QC_STAFF','VIEWER')")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(formulaService.getById(id));
    }

    /**
     * DANH MỤC CÔNG THỨC (CATALOG):
     * - Hiển thị MỖI CÔNG THỨC (Header) CHỈ 1 DÒNG — bản phiên bản mới nhất.
     * - Hỗ trợ tìm kiếm theo q (mã/tên công thức), lọc theo productId.
     * - Phục vụ màn "Danh mục công thức" như yêu cầu KH.
     */
    @GetMapping("/catalog")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','QC_STAFF','VIEWER')")
    @ApiOperation("Danh mục công thức (mỗi công thức chỉ hiển thị phiên bản mới nhất)")
    public ResponseEntity<?> catalogLatest(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "20") int limit
    ) {
        var headersPage = headerRepo.searchHeaders(q, productId, PageRequest.of(start, limit));
        var headerIds = headersPage.getContent().stream().map(FormulaHeader::getId).toList();
        if (headerIds.isEmpty()) {
            return BaseResponse.successListData(List.of(), (int) headersPage.getTotalElements());
        }
        var latest = formulaRepository.findLatestByHeaderIdsWithItemsAndProducts(headerIds); // lấy bản active mới nhất; fallback latest

        // Nếu còn nghi ngờ Lazy ở quan hệ khác, có thể touch:
        // latest.forEach(f -> { if (f.getHeader().getProducts()!=null) f.getHeader().getProducts().size(); });

        var dtos = latest.stream().map(formulaMapper::toDto).toList();
        return BaseResponse.successListData(dtos, (int) headersPage.getTotalElements());
    }

    /**
     * LỊCH SỬ PHIÊN BẢN của 1 công thức (Header) theo formulaCode.
     * - Trả về tất cả version, bản mới nhất đứng trước.
     * - Dùng khi người dùng từ Catalog bấm vào 1 dòng để xem lịch sử/so sánh.
     */
    @GetMapping("/versions/{formulaCode}")
    @PreAuthorize("hasAnyAuthority('ADMIN','PRODUCTION_MANAGER','QC_STAFF','VIEWER')")
    @ApiOperation("Danh sách TOÀN BỘ phiên bản của 1 công thức (theo formulaCode), mới nhất trước")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listVersions(
            @PathVariable String formulaCode,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "20") int limit
    ) {
        var page = formulaRepository.findAllVersions(formulaCode, PageRequest.of(start, limit));
        var dtos = page.getContent().stream().map(formulaMapper::toDto).toList();
        return BaseResponse.successListData(dtos, (int) page.getTotalElements());
    }
}

