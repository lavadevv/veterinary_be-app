package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialBatchDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.BatchQuantityAdjustmentRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.BatchTransferRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.CreateMaterialBatchRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialBatch.UpdateMaterialBatchRequest;
import ext.vnua.veterinary_beapp.modules.material.enums.TestStatus;
import ext.vnua.veterinary_beapp.modules.material.enums.UsageStatus;
import ext.vnua.veterinary_beapp.modules.material.mapper.MaterialBatchMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.material.repository.LocationRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialBatchRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomMaterialBatchQuery;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialBatchService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaterialBatchServiceImpl implements MaterialBatchService {

    private final MaterialBatchRepository materialBatchRepository;
    private final MaterialRepository materialRepository;
    private final LocationRepository locationRepository;
    private final MaterialBatchMapper materialBatchMapper;

    @Override
    public Page<MaterialBatch> getAllMaterialBatch(CustomMaterialBatchQuery.MaterialBatchFilterParam param,
                                                   PageRequest pageRequest) {
        Specification<MaterialBatch> specification = CustomMaterialBatchQuery.getFilterMaterialBatch(param);
        return materialBatchRepository.findAll(specification, pageRequest);
    }

    @Override
    public MaterialBatchDto selectMaterialBatchById(Long id) {
        Optional<MaterialBatch> materialBatchOptional = materialBatchRepository.findById(id);
        if (materialBatchOptional.isEmpty()) {
            throw new DataExistException("Lô vật liệu không tồn tại");
        }
        MaterialBatch materialBatch = materialBatchOptional.get();
        return materialBatchMapper.toMaterialBatchDto(materialBatch);
    }

    @Override
    public MaterialBatchDto selectMaterialBatchByBatchNumber(String batchNumber) {
        Optional<MaterialBatch> materialBatchOptional = materialBatchRepository.findByBatchNumber(batchNumber);
        if (materialBatchOptional.isEmpty()) {
            throw new DataExistException("Số lô không tồn tại");
        }
        MaterialBatch materialBatch = materialBatchOptional.get();
        return materialBatchMapper.toMaterialBatchDto(materialBatch);
    }

    @Override
    public MaterialBatchDto selectMaterialBatchByInternalCode(String internalCode) {
        Optional<MaterialBatch> materialBatchOptional = materialBatchRepository.findByInternalBatchCode(internalCode);
        if (materialBatchOptional.isEmpty()) {
            throw new DataExistException("Mã lô nội bộ không tồn tại");
        }
        MaterialBatch materialBatch = materialBatchOptional.get();
        return materialBatchMapper.toMaterialBatchDto(materialBatch);
    }

    @Override
    public List<MaterialBatchDto> selectMaterialBatchesByMaterial(Long materialId) {
        List<MaterialBatch> materialBatches = materialBatchRepository.findByMaterialId(materialId);
        return materialBatches.stream()
                .map(materialBatchMapper::toMaterialBatchDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<MaterialBatchDto> selectMaterialBatchesByLocation(Long locationId) {
        List<MaterialBatch> materialBatches = materialBatchRepository.findByLocationId(locationId);
        return materialBatches.stream()
                .map(materialBatchMapper::toMaterialBatchDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "MaterialBatch", description = "Tạo mới lô vật liệu")
    public MaterialBatchDto createMaterialBatch(CreateMaterialBatchRequest request) {
        // Validate material exists
        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new DataExistException("Vật liệu không tồn tại"));

        // Validate location exists if provided
        Location location = null;
        if (request.getLocationId() != null) {
            location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new DataExistException("Vị trí không tồn tại"));

            // Check if location is available
            if (!location.getIsAvailable()) {
                throw new MyCustomException("Vị trí không khả dụng");
            }
        }

        // Validate unique internal batch code if provided
        if (request.getInternalBatchCode() != null && !request.getInternalBatchCode().trim().isEmpty()) {
            Optional<MaterialBatch> existingBatch = materialBatchRepository
                    .findByInternalBatchCode(request.getInternalBatchCode());
            if (existingBatch.isPresent()) {
                throw new DataExistException("Mã lô nội bộ đã tồn tại");
            }
        }

        // Validate quantities
        if (request.getReceivedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new MyCustomException("Số lượng nhận phải lớn hơn 0");
        }

        if (request.getCurrentQuantity() != null &&
                request.getCurrentQuantity().compareTo(request.getReceivedQuantity()) > 0) {
            throw new MyCustomException("Số lượng hiện tại không được lớn hơn số lượng nhận");
        }

        // Validate dates
        if (request.getManufacturingDate() != null && request.getExpiryDate() != null &&
                request.getManufacturingDate().isAfter(request.getExpiryDate())) {
            throw new MyCustomException("Ngày sản xuất không được sau ngày hết hạn");
        }

        if (request.getExpiryDate() != null && request.getExpiryDate().isBefore(LocalDate.now())) {
            throw new MyCustomException("Ngày hết hạn không được trong quá khứ");
        }

        try {
            MaterialBatch materialBatch = new MaterialBatch();
            materialBatch.setMaterial(material);
            materialBatch.setLocation(location);
            materialBatch.setBatchNumber(request.getBatchNumber());
            materialBatch.setInternalBatchCode(request.getInternalBatchCode());
            materialBatch.setManufacturerBatchNumber(request.getManufacturerBatchNumber());
            materialBatch.setManufacturingDate(request.getManufacturingDate());
            materialBatch.setExpiryDate(request.getExpiryDate());
            materialBatch.setReceivedDate(request.getReceivedDate());
            materialBatch.setReceivedQuantity(request.getReceivedQuantity());
            materialBatch.setCurrentQuantity(request.getCurrentQuantity() != null ?
                    request.getCurrentQuantity() : request.getReceivedQuantity());
            materialBatch.setUnitPrice(request.getUnitPrice());
            materialBatch.setTestStatus(request.getTestStatus() != null ?
                    request.getTestStatus() : TestStatus.CHO_KIEM_NGHIEM);
            materialBatch.setUsageStatus(request.getUsageStatus() != null ?
                    request.getUsageStatus() : UsageStatus.CACH_LY);
            materialBatch.setCoaNumber(request.getCoaNumber());
            materialBatch.setTestReportNumber(request.getTestReportNumber());
            materialBatch.setTestResults(request.getTestResults());
            materialBatch.setQuarantineReason(request.getQuarantineReason());
            materialBatch.setCoaFilePath(request.getCoaFilePath());
            materialBatch.setMsdsFilePath(request.getMsdsFilePath());
            materialBatch.setTestCertificatePath(request.getTestCertificatePath());
            materialBatch.setNotes(request.getNotes());

            MaterialBatch savedBatch = materialBatchRepository.saveAndFlush(materialBatch);

            // Update location capacity if location is assigned
            if (location != null) {
                updateLocationCapacity(location.getId(), savedBatch.getCurrentQuantity().doubleValue());
            }

            return materialBatchMapper.toMaterialBatchDto(savedBatch);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình thêm lô vật liệu");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "MaterialBatch", description = "Cập nhật lô vật liệu")
    public MaterialBatchDto updateMaterialBatch(UpdateMaterialBatchRequest request) {
        Optional<MaterialBatch> materialBatchOptional = materialBatchRepository.findById(request.getId());
        if (materialBatchOptional.isEmpty()) {
            throw new DataExistException("Lô vật liệu không tồn tại");
        }

        MaterialBatch existingBatch = materialBatchOptional.get();
        Double oldQuantity = existingBatch.getCurrentQuantity().doubleValue();
        Long oldLocationId = existingBatch.getLocation() != null ? existingBatch.getLocation().getId() : null;

        // Validate material exists if changed
        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new DataExistException("Vật liệu không tồn tại"));

        // Validate location exists if provided
        Location location = null;
        if (request.getLocationId() != null) {
            location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new DataExistException("Vị trí không tồn tại"));

            if (!location.getIsAvailable()) {
                throw new MyCustomException("Vị trí không khả dụng");
            }
        }

        // Validate unique internal batch code if changed
        if (request.getInternalBatchCode() != null &&
                !request.getInternalBatchCode().equals(existingBatch.getInternalBatchCode())) {
            Optional<MaterialBatch> duplicateBatch = materialBatchRepository
                    .findByInternalBatchCodeAndIdNot(request.getInternalBatchCode(), request.getId());
            if (duplicateBatch.isPresent()) {
                throw new DataExistException("Mã lô nội bộ đã tồn tại");
            }
        }

        // Validate quantities
        if (request.getReceivedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new MyCustomException("Số lượng nhận phải lớn hơn 0");
        }

        if (request.getCurrentQuantity() != null &&
                request.getCurrentQuantity().compareTo(request.getReceivedQuantity()) > 0) {
            throw new MyCustomException("Số lượng hiện tại không được lớn hơn số lượng nhận");
        }

        // Validate dates
        if (request.getManufacturingDate() != null && request.getExpiryDate() != null &&
                request.getManufacturingDate().isAfter(request.getExpiryDate())) {
            throw new MyCustomException("Ngày sản xuất không được sau ngày hết hạn");
        }

        try {
            // Update fields
            existingBatch.setMaterial(material);
            existingBatch.setLocation(location);
            existingBatch.setBatchNumber(request.getBatchNumber());
            existingBatch.setInternalBatchCode(request.getInternalBatchCode());
            existingBatch.setManufacturerBatchNumber(request.getManufacturerBatchNumber());
            existingBatch.setManufacturingDate(request.getManufacturingDate());
            existingBatch.setExpiryDate(request.getExpiryDate());
            existingBatch.setReceivedDate(request.getReceivedDate());
            existingBatch.setReceivedQuantity(request.getReceivedQuantity());
            existingBatch.setCurrentQuantity(request.getCurrentQuantity() != null ?
                    request.getCurrentQuantity() : request.getReceivedQuantity());
            existingBatch.setUnitPrice(request.getUnitPrice());
            existingBatch.setTestStatus(request.getTestStatus());
            existingBatch.setUsageStatus(request.getUsageStatus());
            existingBatch.setCoaNumber(request.getCoaNumber());
            existingBatch.setTestReportNumber(request.getTestReportNumber());
            existingBatch.setTestResults(request.getTestResults());
            existingBatch.setQuarantineReason(request.getQuarantineReason());
            existingBatch.setCoaFilePath(request.getCoaFilePath());
            existingBatch.setMsdsFilePath(request.getMsdsFilePath());
            existingBatch.setTestCertificatePath(request.getTestCertificatePath());
            existingBatch.setNotes(request.getNotes());

            MaterialBatch savedBatch = materialBatchRepository.saveAndFlush(existingBatch);

            // Update location capacities
            Double newQuantity = savedBatch.getCurrentQuantity().doubleValue();
            Long newLocationId = savedBatch.getLocation() != null ? savedBatch.getLocation().getId() : null;

            // Update old location capacity
            if (oldLocationId != null && !oldLocationId.equals(newLocationId)) {
                updateLocationCapacity(oldLocationId, -oldQuantity);
            }

            // Update new location capacity
            if (newLocationId != null) {
                if (oldLocationId == null || !oldLocationId.equals(newLocationId)) {
                    updateLocationCapacity(newLocationId, newQuantity);
                } else {
                    // Same location, just update the difference
                    updateLocationCapacity(newLocationId, newQuantity - oldQuantity);
                }
            }

            return materialBatchMapper.toMaterialBatchDto(savedBatch);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhật lô vật liệu");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "MaterialBatch", description = "Xóa lô vật liệu")
    public void deleteMaterialBatch(Long id) {
        Optional<MaterialBatch> materialBatchOptional = materialBatchRepository.findById(id);
        if (materialBatchOptional.isEmpty()) {
            throw new DataExistException("Lô vật liệu không tồn tại");
        }

        MaterialBatch materialBatch = materialBatchOptional.get();

        // Check if batch is in use (has positive current quantity)
        if (materialBatch.getCurrentQuantity().compareTo(BigDecimal.ZERO) > 0) {
            throw new MyCustomException("Không thể xóa lô vật liệu còn tồn kho");
        }

        try {
            // Update location capacity if needed
            if (materialBatch.getLocation() != null) {
                updateLocationCapacity(materialBatch.getLocation().getId(),
                        -materialBatch.getCurrentQuantity().doubleValue());
            }

            materialBatchRepository.deleteById(id);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa lô vật liệu");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "MaterialBatch", description = "Xóa danh sách lô vật liệu")
    public List<MaterialBatchDto> deleteAllIdMaterialBatches(List<Long> ids) {
        List<MaterialBatchDto> materialBatchDtos = new ArrayList<>();
        for (Long id : ids) {
            Optional<MaterialBatch> optionalMaterialBatch = materialBatchRepository.findById(id);
            if (optionalMaterialBatch.isPresent()) {
                MaterialBatch materialBatch = optionalMaterialBatch.get();

                // Check if batch is in use
                if (materialBatch.getCurrentQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    throw new MyCustomException("Không thể xóa lô vật liệu còn tồn kho: " + materialBatch.getBatchNumber());
                }

                materialBatchDtos.add(materialBatchMapper.toMaterialBatchDto(materialBatch));

                // Update location capacity if needed
                if (materialBatch.getLocation() != null) {
                    updateLocationCapacity(materialBatch.getLocation().getId(),
                            -materialBatch.getCurrentQuantity().doubleValue());
                }

                materialBatchRepository.delete(materialBatch);
            } else {
                throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa danh sách lô vật liệu!");
            }
        }
        return materialBatchDtos;
    }

    @Override
    @Transactional
    public void updateQuantity(Long batchId, BigDecimal newQuantity) {
        Optional<MaterialBatch> materialBatchOptional = materialBatchRepository.findById(batchId);
        if (materialBatchOptional.isEmpty()) {
            throw new DataExistException("Lô vật liệu không tồn tại");
        }

        MaterialBatch materialBatch = materialBatchOptional.get();

        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new MyCustomException("Số lượng không được âm");
        }

        if (newQuantity.compareTo(materialBatch.getReceivedQuantity()) > 0) {
            throw new MyCustomException("Số lượng hiện tại không được vượt quá số lượng nhận");
        }

        Double oldQuantity = materialBatch.getCurrentQuantity().doubleValue();
        materialBatch.setCurrentQuantity(newQuantity);

        // Update location capacity if location exists
        if (materialBatch.getLocation() != null) {
            Double quantityDifference = newQuantity.doubleValue() - oldQuantity;
            updateLocationCapacity(materialBatch.getLocation().getId(), quantityDifference);
        }

        materialBatchRepository.saveAndFlush(materialBatch);
    }

    @Override
    @Transactional
    public void updateTestStatus(Long batchId, String testStatus) {
        Optional<MaterialBatch> materialBatchOptional = materialBatchRepository.findById(batchId);
        if (materialBatchOptional.isEmpty()) {
            throw new DataExistException("Lô vật liệu không tồn tại");
        }

        MaterialBatch materialBatch = materialBatchOptional.get();

        try {
            TestStatus status = TestStatus.valueOf(testStatus);
            materialBatch.setTestStatus(status);
            materialBatchRepository.saveAndFlush(materialBatch);
        } catch (IllegalArgumentException e) {
            throw new MyCustomException("Trạng thái kiểm nghiệm không hợp lệ");
        }
    }

    @Override
    @Transactional
    public void updateUsageStatus(Long batchId, String usageStatus) {
        Optional<MaterialBatch> materialBatchOptional = materialBatchRepository.findById(batchId);
        if (materialBatchOptional.isEmpty()) {
            throw new DataExistException("Lô vật liệu không tồn tại");
        }

        MaterialBatch materialBatch = materialBatchOptional.get();

        try {
            UsageStatus status = UsageStatus.valueOf(usageStatus);
            materialBatch.setUsageStatus(status);
            materialBatchRepository.saveAndFlush(materialBatch);
        } catch (IllegalArgumentException e) {
            throw new MyCustomException("Trạng thái sử dụng không hợp lệ");
        }
    }

    @Override
    @Transactional
    public void moveToLocation(Long batchId, Long newLocationId) {
        Optional<MaterialBatch> materialBatchOptional = materialBatchRepository.findById(batchId);
        if (materialBatchOptional.isEmpty()) {
            throw new DataExistException("Lô vật liệu không tồn tại");
        }

        MaterialBatch materialBatch = materialBatchOptional.get();
        Long oldLocationId = materialBatch.getLocation() != null ? materialBatch.getLocation().getId() : null;

        Location newLocation = null;
        if (newLocationId != null) {
            newLocation = locationRepository.findById(newLocationId)
                    .orElseThrow(() -> new DataExistException("Vị trí mới không tồn tại"));

            if (!newLocation.getIsAvailable()) {
                throw new MyCustomException("Vị trí mới không khả dụng");
            }
        }

        Double quantity = materialBatch.getCurrentQuantity().doubleValue();

        // Update old location capacity
        if (oldLocationId != null) {
            updateLocationCapacity(oldLocationId, -quantity);
        }

        // Update new location capacity
        if (newLocationId != null) {
            updateLocationCapacity(newLocationId, quantity);
        }

        materialBatch.setLocation(newLocation);
        materialBatchRepository.saveAndFlush(materialBatch);
    }

    @Override
    public List<MaterialBatchDto> getExpiredBatches() {
        List<MaterialBatch> expiredBatches = materialBatchRepository.findExpiredBatches(LocalDate.now());
        return expiredBatches.stream()
                .map(materialBatchMapper::toMaterialBatchDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<MaterialBatchDto> getBatchesNearExpiry() {
        LocalDate currentDate = LocalDate.now();
        LocalDate futureDate = currentDate.plusDays(30);
        List<MaterialBatch> nearExpiryBatches = materialBatchRepository.findBatchesNearExpiry(currentDate, futureDate);
        return nearExpiryBatches.stream()
                .map(materialBatchMapper::toMaterialBatchDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<MaterialBatchDto> getUsableBatches() {
        List<MaterialBatch> usableBatches = materialBatchRepository.findUsableBatches(
                TestStatus.DAT, UsageStatus.SAN_SANG_SU_DUNG, LocalDate.now());
        return usableBatches.stream()
                .map(materialBatchMapper::toMaterialBatchDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public BigDecimal getTotalQuantityByMaterial(Long materialId) {
        BigDecimal totalQuantity = materialBatchRepository.getTotalQuantityByMaterial(materialId);
        return totalQuantity != null ? totalQuantity : BigDecimal.ZERO;
    }

    @Override
    public List<MaterialBatchDto> getOldestUsableBatches(Long materialId) {
        List<MaterialBatch> oldestBatches = materialBatchRepository.findOldestUsableBatches(
                materialId, TestStatus.DAT, UsageStatus.SAN_SANG_SU_DUNG);
        return oldestBatches.stream()
                .map(materialBatchMapper::toMaterialBatchDto)
                .collect(java.util.stream.Collectors.toList());
    }

    // Helper method to update location capacity
    private void updateLocationCapacity(Long locationId, Double capacityChange) {
        if (locationId == null || capacityChange == 0) {
            return;
        }

        Optional<Location> locationOptional = locationRepository.findById(locationId);
        if (locationOptional.isPresent()) {
            Location location = locationOptional.get();
            Double newCapacity = location.getCurrentCapacity() + capacityChange;

            if (newCapacity < 0) {
                newCapacity = 0.0;
            }

            location.setCurrentCapacity(newCapacity);

            // Update availability based on capacity
            if (location.getMaxCapacity() != null) {
                location.setIsAvailable(newCapacity < location.getMaxCapacity());
            }

            locationRepository.saveAndFlush(location);
        }
    }
    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "MaterialBatch", description = "Chuyển lô vật liệu")
    public List<MaterialBatchDto> transferBatches(BatchTransferRequest request) {
        List<MaterialBatchDto> transferredBatches = new ArrayList<>();

        // Validate new location if provided
        Location newLocation = null;
        if (request.getNewLocationId() != null) {
            newLocation = locationRepository.findById(request.getNewLocationId())
                    .orElseThrow(() -> new DataExistException("Vị trí đích không tồn tại"));

            if (!newLocation.getIsAvailable()) {
                throw new MyCustomException("Vị trí đích không khả dụng");
            }
        }

        for (Long batchId : request.getBatchIds()) {
            MaterialBatch batch = materialBatchRepository.findById(batchId)
                    .orElseThrow(() -> new DataExistException("Lô vật liệu không tồn tại: " + batchId));

            Location oldLocation = batch.getLocation();
            Double quantity = batch.getCurrentQuantity().doubleValue();

            // Update old location capacity
            if (oldLocation != null) {
                updateLocationCapacity(oldLocation.getId(), -quantity);
            }

            // Update new location capacity
            if (newLocation != null) {
                updateLocationCapacity(newLocation.getId(), quantity);
            }

            batch.setLocation(newLocation);
            MaterialBatch savedBatch = materialBatchRepository.saveAndFlush(batch);
            transferredBatches.add(materialBatchMapper.toMaterialBatchDto(savedBatch));
        }

        return transferredBatches;
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "MaterialBatch", description = "Điều chỉnh số lượng lô")
    public void adjustBatchQuantity(BatchQuantityAdjustmentRequest request) {
        MaterialBatch batch = materialBatchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new DataExistException("Lô vật liệu không tồn tại"));

        BigDecimal currentQuantity = batch.getCurrentQuantity();
        BigDecimal newQuantity = currentQuantity.add(request.getAdjustmentQuantity());

        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new MyCustomException("Số lượng sau điều chỉnh không được âm");
        }

        if (newQuantity.compareTo(batch.getReceivedQuantity()) > 0) {
            throw new MyCustomException("Số lượng sau điều chỉnh không được vượt quá số lượng nhận");
        }

        batch.setCurrentQuantity(newQuantity);

        // Update notes with adjustment reason
        String adjustmentNote = String.format("Điều chỉnh số lượng: %s (%s) - %s",
                request.getAdjustmentQuantity(), request.getReason(),
                java.time.LocalDateTime.now().toString());

        String existingNotes = batch.getNotes() != null ? batch.getNotes() : "";
        batch.setNotes(existingNotes + "\n" + adjustmentNote);

        // Update location capacity
        if (batch.getLocation() != null) {
            updateLocationCapacity(batch.getLocation().getId(),
                    request.getAdjustmentQuantity().doubleValue());
        }

        // Auto-update usage status based on quantity
        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            batch.setUsageStatus(UsageStatus.DA_HET);
        } else if (batch.getUsageStatus() == UsageStatus.DA_HET) {
            batch.setUsageStatus(UsageStatus.SAN_SANG_SU_DUNG);
        }

        materialBatchRepository.saveAndFlush(batch);
    }

    @Override
    @Transactional
    public void markBatchAsExpired(Long batchId) {
        MaterialBatch batch = materialBatchRepository.findById(batchId)
                .orElseThrow(() -> new DataExistException("Lô vật liệu không tồn tại"));

        batch.setUsageStatus(UsageStatus.HET_HAN);
        batch.setTestStatus(TestStatus.KHONG_DAT);

        String expiredNote = "Đánh dấu hết hạn vào: " + java.time.LocalDateTime.now().toString();
        String existingNotes = batch.getNotes() != null ? batch.getNotes() : "";
        batch.setNotes(existingNotes + "\n" + expiredNote);

        materialBatchRepository.saveAndFlush(batch);
    }

    @Override
    @Transactional
    public void markBatchAsConsumed(Long batchId) {
        MaterialBatch batch = materialBatchRepository.findById(batchId)
                .orElseThrow(() -> new DataExistException("Lô vật liệu không tồn tại"));

        // Update location capacity
        if (batch.getLocation() != null) {
            updateLocationCapacity(batch.getLocation().getId(),
                    -batch.getCurrentQuantity().doubleValue());
        }

        batch.setCurrentQuantity(BigDecimal.ZERO);
        batch.setUsageStatus(UsageStatus.DA_HET);

        String consumedNote = "Đánh dấu đã sử dụng hết vào: " + java.time.LocalDateTime.now().toString();
        String existingNotes = batch.getNotes() != null ? batch.getNotes() : "";
        batch.setNotes(existingNotes + "\n" + consumedNote);

        materialBatchRepository.saveAndFlush(batch);
    }

    @Override
    public List<MaterialBatchDto> getLowStockBatches(BigDecimal threshold) {
        List<MaterialBatch> allBatches = materialBatchRepository.findBatchesWithStock();
        return allBatches.stream()
                .filter(batch -> batch.getCurrentQuantity().compareTo(threshold) <= 0)
                .map(materialBatchMapper::toMaterialBatchDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<MaterialBatchDto> getBatchesByDateRange(LocalDate startDate, LocalDate endDate) {
        // This would need a custom query in repository
        CustomMaterialBatchQuery.MaterialBatchFilterParam param =
                new CustomMaterialBatchQuery.MaterialBatchFilterParam();
        param.setReceivedFromDate(startDate);
        param.setReceivedToDate(endDate);

        Specification<MaterialBatch> specification = CustomMaterialBatchQuery.getFilterMaterialBatch(param);
        List<MaterialBatch> batches = materialBatchRepository.findAll(specification);

        return batches.stream()
                .map(materialBatchMapper::toMaterialBatchDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public BigDecimal calculateTotalValue() {
        List<MaterialBatch> allBatches = materialBatchRepository.findBatchesWithStock();
        return allBatches.stream()
                .filter(batch -> batch.getUnitPrice() != null)
                .map(batch -> batch.getCurrentQuantity().multiply(batch.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateTotalValueByMaterial(Long materialId) {
        List<MaterialBatch> batches = materialBatchRepository.findByMaterialId(materialId);
        return batches.stream()
                .filter(batch -> batch.getUnitPrice() != null &&
                        batch.getCurrentQuantity().compareTo(BigDecimal.ZERO) > 0)
                .map(batch -> batch.getCurrentQuantity().multiply(batch.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<MaterialBatchDto> getPendingTestBatches() {
        List<MaterialBatch> pendingBatches = materialBatchRepository.findByTestStatus(TestStatus.CHO_KIEM_NGHIEM);
        List<MaterialBatch> testingBatches = materialBatchRepository.findByTestStatus(TestStatus.DANG_CACH_LY);

        List<MaterialBatch> allPending = new ArrayList<>(pendingBatches);
        allPending.addAll(testingBatches);

        return allPending.stream()
                .map(materialBatchMapper::toMaterialBatchDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<MaterialBatchDto> getFailedTestBatches() {
        List<MaterialBatch> failedBatches = materialBatchRepository.findByTestStatus(TestStatus.KHONG_DAT);
        return failedBatches.stream()
                .map(materialBatchMapper::toMaterialBatchDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "MaterialBatch", description = "Duyệt kiểm nghiệm lô")
    public void approveTestBatch(Long batchId, String testResults) {
        MaterialBatch batch = materialBatchRepository.findById(batchId)
                .orElseThrow(() -> new DataExistException("Lô vật liệu không tồn tại"));

        batch.setTestStatus(TestStatus.DAT);
        batch.setUsageStatus(UsageStatus.SAN_SANG_SU_DUNG);
        batch.setTestResults(testResults);

        String approvalNote = "Duyệt kiểm nghiệm vào: " + java.time.LocalDateTime.now().toString();
        String existingNotes = batch.getNotes() != null ? batch.getNotes() : "";
        batch.setNotes(existingNotes + "\n" + approvalNote);

        materialBatchRepository.saveAndFlush(batch);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "MaterialBatch", description = "Từ chối kiểm nghiệm lô")
    public void rejectTestBatch(Long batchId, String rejectionReason) {
        MaterialBatch batch = materialBatchRepository.findById(batchId)
                .orElseThrow(() -> new DataExistException("Lô vật liệu không tồn tại"));

        batch.setTestStatus(TestStatus.KHONG_DAT);
        batch.setUsageStatus(UsageStatus.BI_CAM);
        batch.setQuarantineReason(rejectionReason);

        String rejectionNote = "Từ chối kiểm nghiệm vào: " + java.time.LocalDateTime.now().toString()
                + " - Lý do: " + rejectionReason;
        String existingNotes = batch.getNotes() != null ? batch.getNotes() : "";
        batch.setNotes(existingNotes + "\n" + rejectionNote);

        materialBatchRepository.saveAndFlush(batch);
    }
}
