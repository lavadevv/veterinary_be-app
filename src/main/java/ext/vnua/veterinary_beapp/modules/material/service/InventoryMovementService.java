// File: ext/vnua/veterinary_beapp/modules/material/service/InventoryMovementService.java
package ext.vnua.veterinary_beapp.modules.material.service;

import java.math.BigDecimal;

/**
 * Dịch vụ thao tác kho: xuất dùng, chuyển vị trí/kho, giữ/hoàn giữ chỗ,
 * và đồng bộ sức chứa vị trí.
 */
public interface InventoryMovementService {

    /**
     * Xuất dùng một lượng từ batch.
     * - Trừ currentQuantity
     * - Recalc availableQuantity = current - reserved (không âm)
     * - Cập nhật currentCapacity & isAvailable của Location chứa batch
     * - Ghi nhật ký MaterialMovement(CONSUME)
     */
    void consumeFromBatch(Long batchId, BigDecimal quantity, String note);

    /**
     * Chuyển toàn bộ batch sang vị trí khác.
     * - Kiểm tra sức chứa vị trí đích
     * - Cập nhật Location của batch
     * - Đồng bộ sức chứa vị trí cũ/mới
     * - Ghi nhật ký MaterialMovement(TRANSFER)
     */
    void moveBatchAll(Long batchId, Long toLocationId, String note);

    /**
     * Chuyển một phần batch sang vị trí khác (tạo batch mới tại vị trí đích).
     * - Kiểm tra sức chứa vị trí đích
     * - Tạo batch mới với số lượng chuyển
     * - Giảm batch nguồn tương ứng
     * - Đồng bộ sức chứa 2 vị trí
     * - Ghi nhật ký MaterialMovement(TRANSFER)
     * @return id batch mới
     */
    Long moveBatchPartially(Long batchId, Long toLocationId, BigDecimal quantity, String note);

    /**
     * Giữ chỗ trong batch (tác động reservedQuantity).
     * - Kiểm tra không vượt currentQuantity
     * - Recalc availableQuantity
     * - Cập nhật sức chứa vị trí
     * - Ghi nhật ký MaterialMovement(RESERVE)
     */
    void reserve(Long batchId, BigDecimal quantity, String note);

    /**
     * Hoàn/huỷ giữ chỗ (giảm reservedQuantity).
     * - Kiểm tra không vượt reserved hiện tại
     * - Recalc availableQuantity
     * - Cập nhật sức chứa vị trí
     * - Ghi nhật ký MaterialMovement(RELEASE)
     */
    void releaseReserve(Long batchId, BigDecimal quantity, String note);

    /**
     * Tính lại sức chứa của vị trí = tổng currentQuantity các batch tại vị trí.
     * - Cập nhật currentCapacity
     * - isAvailable = currentCapacity < maxCapacity (nếu có max)
     */
    void recomputeLocationCapacity(Long locationId);
}
