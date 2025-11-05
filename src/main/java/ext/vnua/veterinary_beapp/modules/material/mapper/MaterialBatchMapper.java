package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchDto;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for MaterialBatch container structure.
 * Maps batch container and its items (MaterialBatchItem).
 */
@Mapper(componentModel = "spring", uses = {MaterialBatchItemMapper.class})
public interface MaterialBatchMapper {
    MaterialBatchMapper INSTANCE = Mappers.getMapper(MaterialBatchMapper.class);

    // Map batch container with full items
    @Mapping(target = "materialId", ignore = true)  // No direct material
    @Mapping(source = "location.id", target = "locationId")
    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "manufacturer.id", target = "manufacturerId")
    @Mapping(target = "materialName", ignore = true)  // No direct material
    @Mapping(target = "materialCode", ignore = true)  // No direct material
    @Mapping(target = "internationalName", ignore = true)  // No direct material
    @Mapping(target = "unitName", ignore = true)  // No direct material
    @Mapping(target = "formTypeName", ignore = true)  // No direct material
    @Mapping(source = "supplier.supplierName", target = "supplierName")
    @Mapping(source = "manufacturer.manufacturerName", target = "manufacturerName")
    @Mapping(source = "location.locationCode", target = "locationCode")
    // Note: activeIngredients field removed from MaterialBatchDto - now in MaterialBatchItemActiveIngredient
    @Mapping(target = "receivedQuantity", ignore = true) // Now in items
    @Mapping(target = "currentQuantity", ignore = true)  // Now in items
    @Mapping(target = "unitPrice", ignore = true)  // Now in items
    @Mapping(target = "taxPercent", ignore = true)  // Now in items
    @Mapping(target = "subtotalAmount", ignore = true)  // Now in items
    @Mapping(target = "taxAmount", ignore = true)  // Now in items
    @Mapping(target = "testStatus", ignore = true)  // Now in items
    @Mapping(target = "usageStatus", ignore = true)  // Now in items
    @Mapping(target = "manufacturerBatchNumber", ignore = true)  // Now in items
    @Mapping(target = "manufacturingDate", ignore = true)  // Now in items
    @Mapping(target = "expiryDate", ignore = true)  // Now in items
    @Mapping(target = "reservedQuantity", ignore = true)  // Now in items
    @Mapping(target = "availableQuantity", ignore = true)  // Now in items
    @Mapping(target = "shelfLocation", ignore = true)  // Now in items
    @Mapping(target = "imagePath", ignore = true)  // Now in items
    @Mapping(target = "coaNumber", ignore = true)  // Now in items
    @Mapping(target = "testReportNumber", ignore = true)  // Now in items
    @Mapping(target = "testResults", ignore = true)  // Now in items
    @Mapping(target = "quarantineReason", ignore = true)  // Now in items
    @Mapping(target = "coaFilePath", ignore = true)  // Now in items
    @Mapping(target = "msdsFilePath", ignore = true)  // Now in items
    @Mapping(target = "testCertificatePath", ignore = true)  // Now in items
    @Mapping(target = "batchItemIds", expression = "java(materialBatch.getBatchItems() != null ? materialBatch.getBatchItems().stream().map(item -> item.getId()).collect(java.util.stream.Collectors.toList()) : null)")
    @Mapping(target = "totalItemsCount", expression = "java(materialBatch.getBatchItems() != null ? materialBatch.getBatchItems().size() : 0)")
    @Mapping(source = "batchItems", target = "batchItems") // Map full items using MaterialBatchItemMapper
    @Mapping(target = "totalCurrentQuantity", expression = "java(calculateTotalCurrentQuantity(materialBatch))")
    @Mapping(target = "readyItemsCount", expression = "java(calculateReadyItemsCount(materialBatch))")
    @Mapping(target = "quarantinedItemsCount", expression = "java(calculateQuarantinedItemsCount(materialBatch))")
    @Mapping(target = "nearExpiryItemsCount", expression = "java(calculateNearExpiryItemsCount(materialBatch))")
    @Mapping(target = "expiredItemsCount", expression = "java(calculateExpiredItemsCount(materialBatch))")
    MaterialBatchDto toMaterialBatchDto(MaterialBatch materialBatch);

    @Mapping(target = "location", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "manufacturer", ignore = true)
    @Mapping(target = "batchItems", ignore = true)  // Changed from batchActiveIngredients
    @Mapping(target = "batchStatus", ignore = true)  // Business logic field
    @Mapping(target = "unqualifiedItems", ignore = true)  // Computed method, not a field
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    MaterialBatch toMaterialBatch(MaterialBatchDto materialBatchDto);
    
    // ===== HELPER METHODS FOR COMPUTED FIELDS =====
    
    /**
     * Tính tổng số lượng hiện tại từ tất cả items
     */
    default java.math.BigDecimal calculateTotalCurrentQuantity(MaterialBatch batch) {
        if (batch.getBatchItems() == null || batch.getBatchItems().isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }
        return batch.getBatchItems().stream()
            .map(item -> item.getCurrentQuantity() != null ? item.getCurrentQuantity() : java.math.BigDecimal.ZERO)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
    
    /**
     * Đếm số items sẵn sàng (testStatus=DAT && usageStatus=SAN_SANG)
     */
    default Integer calculateReadyItemsCount(MaterialBatch batch) {
        if (batch.getBatchItems() == null || batch.getBatchItems().isEmpty()) {
            return 0;
        }
        return (int) batch.getBatchItems().stream()
            .filter(item -> "DAT".equals(item.getTestStatus() != null ? item.getTestStatus().name() : null) 
                         && "SAN_SANG".equals(item.getUsageStatus() != null ? item.getUsageStatus().name() : null))
            .count();
    }
    
    /**
     * Đếm số items cách ly (testStatus=KHONG_DAT || usageStatus=CACH_LY)
     */
    default Integer calculateQuarantinedItemsCount(MaterialBatch batch) {
        if (batch.getBatchItems() == null || batch.getBatchItems().isEmpty()) {
            return 0;
        }
        return (int) batch.getBatchItems().stream()
            .filter(item -> {
                String testStatus = item.getTestStatus() != null ? item.getTestStatus().name() : null;
                String usageStatus = item.getUsageStatus() != null ? item.getUsageStatus().name() : null;
                return "KHONG_DAT".equals(testStatus) || "CACH_LY".equals(usageStatus);
            })
            .count();
    }
    
    /**
     * Đếm số items sắp hết hạn (trong vòng 30 ngày)
     */
    default Integer calculateNearExpiryItemsCount(MaterialBatch batch) {
        if (batch.getBatchItems() == null || batch.getBatchItems().isEmpty()) {
            return 0;
        }
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate threshold = now.plusDays(30);
        
        return (int) batch.getBatchItems().stream()
            .filter(item -> {
                java.time.LocalDate expiryDate = item.getExpiryDate();
                if (expiryDate == null) return false;
                return !expiryDate.isBefore(now) && !expiryDate.isAfter(threshold);
            })
            .count();
    }
    
    /**
     * Đếm số items đã hết hạn
     */
    default Integer calculateExpiredItemsCount(MaterialBatch batch) {
        if (batch.getBatchItems() == null || batch.getBatchItems().isEmpty()) {
            return 0;
        }
        java.time.LocalDate now = java.time.LocalDate.now();
        
        return (int) batch.getBatchItems().stream()
            .filter(item -> {
                java.time.LocalDate expiryDate = item.getExpiryDate();
                return expiryDate != null && expiryDate.isBefore(now);
            })
            .count();
    }
}