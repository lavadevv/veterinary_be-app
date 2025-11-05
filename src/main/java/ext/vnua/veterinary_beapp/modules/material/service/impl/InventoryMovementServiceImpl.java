package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.material.enums.MovementType;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialMovement;
import ext.vnua.veterinary_beapp.modules.material.repository.LocationRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialBatchRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialMovementRepository;
import ext.vnua.veterinary_beapp.modules.material.service.InventoryMovementService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InventoryMovementServiceImpl implements InventoryMovementService {

    private static final int QTY_SCALE = 3;

    private final EntityManager em;
    private final MaterialBatchRepository batchRepo;
    private final LocationRepository locationRepo;
    private final MaterialMovementRepository movementRepo;

    // ====================== PUBLIC API ======================

    @Override
    @Transactional
    public void consumeFromBatch(Long batchId, BigDecimal quantity, String note) {
        // TODO: Refactor to work with MaterialBatchItem
        // MaterialBatch no longer has availableQuantity, currentQuantity, reservedQuantity
        // These fields are now on MaterialBatchItem
        // Need to:
        // 1. Accept materialBatchItemId instead of batchId
        // 2. Query MaterialBatchItem and check availableQuantity
        // 3. Use MaterialBatchItem.updateQuantity() method
        // 4. Update location capacity based on item's location
        throw new UnsupportedOperationException(
                "This method needs refactoring to work with MaterialBatchItem. " +
                "Quantities are now tracked per MaterialBatchItem, not MaterialBatch.");
    }

    @Override
    @Transactional
    public void moveBatchAll(Long batchId, Long toLocationId, String note) {
        // TODO: Refactor to move MaterialBatchItems, not container
        // MaterialBatch is now just a container
        // Need to iterate through batch.getBatchItems() and move each item individually
        throw new UnsupportedOperationException(
                "This method needs refactoring to work with MaterialBatchItem. " +
                "Should move individual MaterialBatchItems, not the container.");
    }

    @Override
    @Transactional
    public Long moveBatchPartially(Long batchId, Long toLocationId, BigDecimal quantity, String note) {
        // TODO: Refactor to work with MaterialBatchItem
        // MaterialBatch no longer has material, quantity, price fields - these are on MaterialBatchItem
        // Should split a MaterialBatchItem instead of MaterialBatch
        throw new UnsupportedOperationException(
                "This method needs refactoring to work with MaterialBatchItem. " +
                "Should split/move individual MaterialBatchItems.");
    }

    @Override
    @Transactional
    public void reserve(Long batchId, BigDecimal quantity, String note) {
        // TODO: Refactor to reserve MaterialBatchItem
        // Use MaterialBatchItem.reserve() method
        throw new UnsupportedOperationException(
                "This method needs refactoring to work with MaterialBatchItem. " +
                "Use MaterialBatchItem.reserve() method instead.");
    }

    @Override
    @Transactional
    public void releaseReserve(Long batchId, BigDecimal quantity, String note) {
        // TODO: Refactor to release MaterialBatchItem reservation
        // Use MaterialBatchItem.release() method
        throw new UnsupportedOperationException(
                "This method needs refactoring to work with MaterialBatchItem. " +
                "Use MaterialBatchItem.release() method instead.");
    }

    @Override
    @Transactional
    public void recomputeLocationCapacity(Long locationId) {
        Location loc = lockLocation(locationId);
        BigDecimal used = sumLocationUsage(loc.getId());
        double usedDouble = used.setScale(QTY_SCALE, RoundingMode.HALF_UP).doubleValue();
        loc.setCurrentCapacity(usedDouble);
        // trạng thái sẵn sàng ~ còn chỗ
        if (loc.getMaxCapacity() != null) {
            loc.setIsAvailable(usedDouble < loc.getMaxCapacity());
        }
        locationRepo.saveAndFlush(loc);
    }

    // ====================== HELPERS ======================

    private MaterialBatch lockBatch(Long id) {
        MaterialBatch b = em.find(MaterialBatch.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (b == null) throw new MyCustomException("Không tìm thấy lô (id=" + id + ")");
        return b;
    }

    private Location lockLocation(Long id) {
        Location l = em.find(Location.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (l == null) throw new MyCustomException("Không tìm thấy vị trí (id=" + id + ")");
        return l;
    }

    private void checkCapacity(Location dst, BigDecimal afterUsed) {
        if (dst.getMaxCapacity() != null) {
            BigDecimal max = BigDecimal.valueOf(dst.getMaxCapacity());
            if (afterUsed.compareTo(max) > 0) {
                throw new MyCustomException("Vượt sức chứa vị trí đích");
            }
        }
    }

    private BigDecimal sumLocationUsage(Long locationId) {
        // TODO: Refactor to sum from MaterialBatchItem.currentQuantity
        // JPQL should query MaterialBatchItem instead of MaterialBatch
        // Example: "select coalesce(sum(item.currentQuantity), 0) from MaterialBatchItem item where item.location.id = :locId"
        throw new UnsupportedOperationException(
                "Location capacity calculation needs to sum MaterialBatchItem quantities, not MaterialBatch.");
    }

    private void persistMovement(MovementType type,
                                 MaterialBatch src, MaterialBatch dst,
                                 Location srcLoc, Location dstLoc,
                                 BigDecimal qty, String note) {
        MaterialMovement mv = new MaterialMovement();
        mv.setMovementType(type);
        mv.setSourceBatch(src);
        mv.setTargetBatch(dst);
        mv.setSourceLocation(srcLoc);
        mv.setTargetLocation(dstLoc);
        mv.setQuantity(qty.setScale(QTY_SCALE, RoundingMode.HALF_UP));
        mv.setNote(note);
        movementRepo.save(mv);
    }

    private void requirePositive(BigDecimal v, String msg) {
        if (v == null || v.signum() <= 0) throw new MyCustomException(msg);
    }
}
