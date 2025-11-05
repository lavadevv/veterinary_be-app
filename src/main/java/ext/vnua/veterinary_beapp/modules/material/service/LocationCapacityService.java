package ext.vnua.veterinary_beapp.modules.material.service;

import java.math.BigDecimal;

/**
 * Service interface for managing warehouse location capacity
 * Handles real-time capacity tracking when material batches are created, updated, moved or deleted
 */
public interface LocationCapacityService {

    /**
     * Add quantity to location capacity when creating or moving a batch to location
     *
     * @param locationId ID of the location
     * @param quantity   Quantity to add (can use BigDecimal or Double)
     * @throws RuntimeException if location not found or capacity exceeded
     */
    void addBatchToLocation(Long locationId, BigDecimal quantity);

    /**
     * Remove quantity from location capacity when deleting or moving a batch from location
     *
     * @param locationId ID of the location
     * @param quantity   Quantity to remove
     * @throws RuntimeException if location not found
     */
    void removeBatchFromLocation(Long locationId, BigDecimal quantity);

    /**
     * Move batch between locations - removes from old location and adds to new location
     *
     * @param fromLocationId Source location ID (can be null)
     * @param toLocationId   Destination location ID (can be null)
     * @param quantity       Quantity to move
     * @throws RuntimeException if locations not found or capacity exceeded
     */
    void moveBatch(Long fromLocationId, Long toLocationId, BigDecimal quantity);

    /**
     * Update location capacity when batch quantity changes
     *
     * @param locationId  ID of the location
     * @param oldQuantity Previous quantity
     * @param newQuantity New quantity
     * @throws RuntimeException if location not found or capacity exceeded
     */
    void updateBatchQuantity(Long locationId, BigDecimal oldQuantity, BigDecimal newQuantity);

    /**
     * Recalculate total occupied capacity for a location based on all its batches
     * Useful for fixing inconsistencies
     *
     * @param locationId ID of the location
     * @throws RuntimeException if location not found
     */
    void recalculateLocationCapacity(Long locationId);

    /**
     * Check if location has enough available capacity
     *
     * @param locationId ID of the location
     * @param quantity   Quantity to check
     * @return true if location has enough capacity, false otherwise
     */
    boolean hasAvailableCapacity(Long locationId, BigDecimal quantity);

    /**
     * Get available capacity for a location
     *
     * @param locationId ID of the location
     * @return Available capacity (maxCapacity - currentCapacity)
     */
    Double getAvailableCapacity(Long locationId);

    /**
     * Get occupancy percentage for a location
     *
     * @param locationId ID of the location
     * @return Occupancy percentage (0-100)
     */
    Double getOccupancyPercentage(Long locationId);
}
