package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.StockAlertDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.stockAlert.CreateStockAlertRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.stockAlert.UpdateStockAlertRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.StockAlertMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.material.model.StockAlert;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialBatchRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.StockAlertRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomStockAlertQuery;
import ext.vnua.veterinary_beapp.modules.material.service.StockAlertService;
import ext.vnua.veterinary_beapp.modules.users.model.User;
import ext.vnua.veterinary_beapp.modules.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockAlertServiceImpl implements StockAlertService {
    private final StockAlertRepository stockAlertRepository;
    private final MaterialRepository materialRepository;
    private final MaterialBatchRepository materialBatchRepository;
    private final UserRepository userRepository;
    private final StockAlertMapper stockAlertMapper;

    @Override
    public Page<StockAlert> getAllStockAlert(CustomStockAlertQuery.StockAlertFilterParam param, PageRequest pageRequest) {
        Specification<StockAlert> specification = CustomStockAlertQuery.getFilterStockAlert(param);
        return stockAlertRepository.findAll(specification, pageRequest);
    }

    @Override
    public StockAlertDto selectStockAlertById(Long id) {
        Optional<StockAlert> stockAlertOptional = stockAlertRepository.findById(id);
        if (stockAlertOptional.isEmpty()) {
            throw new DataExistException("Cảnh báo kho không tồn tại");
        }
        StockAlert stockAlert = stockAlertOptional.get();
        return stockAlertMapper.toStockAlertDto(stockAlert);
    }

    @Override
    public List<StockAlertDto> selectStockAlertsByMaterial(Long materialId) {
        List<StockAlert> stockAlerts = stockAlertRepository.findByMaterialId(materialId);
        return stockAlerts.stream()
                .map(stockAlertMapper::toStockAlertDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<StockAlertDto> selectUnresolvedAlerts() {
        List<StockAlert> stockAlerts = stockAlertRepository.findByIsResolvedFalse();
        return stockAlerts.stream()
                .map(stockAlertMapper::toStockAlertDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "StockAlert", description = "Tạo mới cảnh báo kho")
    public StockAlertDto createStockAlert(CreateStockAlertRequest request) {
        // Validate material exists
        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new DataExistException("Vật liệu không tồn tại"));

        // Validate material batch exists if provided
        MaterialBatch materialBatch = null;
        if (request.getMaterialBatchId() != null) {
            materialBatch = materialBatchRepository.findById(request.getMaterialBatchId())
                    .orElseThrow(() -> new DataExistException("Lô vật liệu không tồn tại"));
        }

        // Check for duplicate alerts
        List<StockAlert> duplicateAlerts;
        if (materialBatch != null) {
            duplicateAlerts = stockAlertRepository.findDuplicateBatchAlerts(
                    request.getMaterialBatchId(), request.getAlertType());
        } else {
            duplicateAlerts = stockAlertRepository.findDuplicateAlerts(
                    request.getMaterialId(), request.getAlertType());
        }

        if (!duplicateAlerts.isEmpty()) {
            throw new DataExistException("Cảnh báo cùng loại cho vật liệu này đã tồn tại và chưa được giải quyết");
        }

        try {
            StockAlert stockAlert = new StockAlert();
            stockAlert.setMaterial(material);
            stockAlert.setMaterialBatch(materialBatch);
            stockAlert.setAlertType(request.getAlertType());
            stockAlert.setAlertMessage(request.getAlertMessage());
            stockAlert.setAlertDate(LocalDateTime.now());
            stockAlert.setIsResolved(false);

            return stockAlertMapper.toStockAlertDto(stockAlertRepository.saveAndFlush(stockAlert));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình tạo cảnh báo kho");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "StockAlert", description = "Cập nhật cảnh báo kho")
    public StockAlertDto updateStockAlert(UpdateStockAlertRequest request) {
        Optional<StockAlert> stockAlertOptional = stockAlertRepository.findById(request.getId());
        if (stockAlertOptional.isEmpty()) {
            throw new DataExistException("Cảnh báo kho không tồn tại");
        }

        StockAlert existingStockAlert = stockAlertOptional.get();

        // Validate material exists if changed
        if (request.getMaterialId() != null &&
                !request.getMaterialId().equals(existingStockAlert.getMaterial().getId())) {
            Material material = materialRepository.findById(request.getMaterialId())
                    .orElseThrow(() -> new DataExistException("Vật liệu không tồn tại"));
            existingStockAlert.setMaterial(material);
        }

        // Validate material batch exists if provided/changed
        if (request.getMaterialBatchId() != null) {
            if (existingStockAlert.getMaterialBatch() == null ||
                    !request.getMaterialBatchId().equals(existingStockAlert.getMaterialBatch().getId())) {
                MaterialBatch materialBatch = materialBatchRepository.findById(request.getMaterialBatchId())
                        .orElseThrow(() -> new DataExistException("Lô vật liệu không tồn tại"));
                existingStockAlert.setMaterialBatch(materialBatch);
            }
        }

        try {
            if (request.getAlertType() != null) {
                existingStockAlert.setAlertType(request.getAlertType());
            }
            if (request.getAlertMessage() != null) {
                existingStockAlert.setAlertMessage(request.getAlertMessage());
            }
            if (request.getIsResolved() != null) {
                existingStockAlert.setIsResolved(request.getIsResolved());
                if (request.getIsResolved()) {
                    existingStockAlert.setResolvedDate(LocalDateTime.now());
                } else {
                    existingStockAlert.setResolvedDate(null);
                    existingStockAlert.setResolvedBy(null);
                    existingStockAlert.setResolutionNotes(null);
                }
            }
            if (request.getResolutionNotes() != null) {
                existingStockAlert.setResolutionNotes(request.getResolutionNotes());
            }

            return stockAlertMapper.toStockAlertDto(stockAlertRepository.saveAndFlush(existingStockAlert));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhật cảnh báo kho");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "StockAlert", description = "Xóa cảnh báo kho")
    public void deleteStockAlert(Long id) {
        Optional<StockAlert> stockAlertOptional = stockAlertRepository.findById(id);
        if (stockAlertOptional.isEmpty()) {
            throw new DataExistException("Cảnh báo kho không tồn tại");
        }

        try {
            stockAlertRepository.deleteById(id);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa cảnh báo kho");
        }
    }

    @Override
    @Auditable(action = AuditAction.DELETE, entityName = "StockAlert", description = "Xóa danh sách cảnh báo kho")
    public List<StockAlertDto> deleteAllIdStockAlerts(List<Long> ids) {
        List<StockAlertDto> stockAlertDtos = new ArrayList<>();
        for (Long id : ids) {
            Optional<StockAlert> optionalStockAlert = stockAlertRepository.findById(id);
            if (optionalStockAlert.isPresent()) {
                StockAlert stockAlert = optionalStockAlert.get();
                stockAlertDtos.add(stockAlertMapper.toStockAlertDto(stockAlert));
                stockAlertRepository.delete(stockAlert);
            } else {
                throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa danh sách cảnh báo kho!");
            }
        }
        return stockAlertDtos;
    }

    @Override
    @Transactional
    public StockAlertDto resolveAlert(Long alertId, Long userId, String resolutionNotes) {
        Optional<StockAlert> stockAlertOptional = stockAlertRepository.findById(alertId);
        if (stockAlertOptional.isEmpty()) {
            throw new DataExistException("Cảnh báo kho không tồn tại");
        }

        StockAlert stockAlert = stockAlertOptional.get();

        if (stockAlert.getIsResolved()) {
            throw new MyCustomException("Cảnh báo này đã được giải quyết");
        }

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataExistException("Người dùng không tồn tại"));

        stockAlert.setIsResolved(true);
        stockAlert.setResolvedDate(LocalDateTime.now());
        stockAlert.setResolvedBy(user);
        stockAlert.setResolutionNotes(resolutionNotes);

        return stockAlertMapper.toStockAlertDto(stockAlertRepository.saveAndFlush(stockAlert));
    }

    @Override
    @Transactional
    public void createLowStockAlert(Long materialId, Double currentStock, Double minThreshold) {
        // Check if alert already exists for this material
        List<StockAlert> existingAlerts = stockAlertRepository.findDuplicateAlerts(materialId, StockAlert.AlertType.LOW_STOCK);
        if (!existingAlerts.isEmpty()) {
            return; // Don't create duplicate alert
        }

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new DataExistException("Vật liệu không tồn tại"));

        StockAlert stockAlert = new StockAlert();
        stockAlert.setMaterial(material);
        stockAlert.setAlertType(StockAlert.AlertType.LOW_STOCK);
        stockAlert.setAlertMessage(String.format("Tồn kho thấp: %s (Còn: %.2f, Tối thiểu: %.2f)",
                material.getMaterialName(), currentStock, minThreshold));
        stockAlert.setAlertDate(LocalDateTime.now());
        stockAlert.setIsResolved(false);

        stockAlertRepository.saveAndFlush(stockAlert);
    }

    @Override
    @Transactional
    public void createExpiryAlert(Long materialBatchId, LocalDateTime expiryDate) {
        List<StockAlert> existingAlerts = stockAlertRepository.findDuplicateBatchAlerts(materialBatchId, StockAlert.AlertType.EXPIRED);
        if (!existingAlerts.isEmpty()) {
            return;
        }

        MaterialBatch materialBatch = materialBatchRepository.findById(materialBatchId)
                .orElseThrow(() -> new DataExistException("Lô vật liệu không tồn tại"));

        // TODO: REFACTOR - MaterialBatch no longer has direct Material reference
        // Need to iterate through batchItems to create alerts for each material
        throw new UnsupportedOperationException(
            "Alert creation needs refactoring for new MaterialBatchItem structure");
        
        /*
        StockAlert stockAlert = new StockAlert();
        stockAlert.setMaterial(materialBatch.getMaterial());  // NO LONGER VALID
        stockAlert.setMaterialBatch(materialBatch);
        stockAlert.setAlertType(StockAlert.AlertType.EXPIRED);
        stockAlert.setAlertMessage(String.format("Lô hàng đã hết hạn: %s (Hết hạn: %s)",
                materialBatch.getBatchNumber(), expiryDate.toString()));
        stockAlert.setAlertDate(LocalDateTime.now());
        stockAlert.setIsResolved(false);

        stockAlertRepository.saveAndFlush(stockAlert);
        */
    }

    @Override
    @Transactional
    public void createNearExpiryAlert(Long materialBatchId, LocalDateTime expiryDate, int daysBeforeExpiry) {
        List<StockAlert> existingAlerts = stockAlertRepository.findDuplicateBatchAlerts(materialBatchId, StockAlert.AlertType.NEAR_EXPIRY);
        if (!existingAlerts.isEmpty()) {
            return;
        }

        MaterialBatch materialBatch = materialBatchRepository.findById(materialBatchId)
                .orElseThrow(() -> new DataExistException("Lô vật liệu không tồn tại"));

        // TODO: REFACTOR - MaterialBatch no longer has direct Material reference
        throw new UnsupportedOperationException(
            "Alert creation needs refactoring for new MaterialBatchItem structure");
        
        /*
        StockAlert stockAlert = new StockAlert();
        stockAlert.setMaterial(materialBatch.getMaterial());  // NO LONGER VALID
        stockAlert.setMaterialBatch(materialBatch);
        stockAlert.setAlertType(StockAlert.AlertType.NEAR_EXPIRY);
        stockAlert.setAlertMessage(String.format("Lô hàng sắp hết hạn: %s (Hết hạn trong %d ngày)",
                materialBatch.getBatchNumber(), daysBeforeExpiry));
        stockAlert.setAlertDate(LocalDateTime.now());
        stockAlert.setIsResolved(false);

        stockAlertRepository.saveAndFlush(stockAlert);
        */
    }

    @Override
    @Transactional
    public void createNegativeStockAlert(Long materialId, Double currentStock) {
        List<StockAlert> existingAlerts = stockAlertRepository.findDuplicateAlerts(materialId, StockAlert.AlertType.NEGATIVE_STOCK);
        if (!existingAlerts.isEmpty()) {
            return;
        }

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new DataExistException("Vật liệu không tồn tại"));

        StockAlert stockAlert = new StockAlert();
        stockAlert.setMaterial(material);
        stockAlert.setAlertType(StockAlert.AlertType.NEGATIVE_STOCK);
        stockAlert.setAlertMessage(String.format("Tồn kho âm: %s (Số lượng: %.2f)",
                material.getMaterialName(), currentStock));
        stockAlert.setAlertDate(LocalDateTime.now());
        stockAlert.setIsResolved(false);

        stockAlertRepository.saveAndFlush(stockAlert);
    }

    @Override
    @Transactional
    public void createQuarantineAlert(Long materialBatchId, String reason) {
        List<StockAlert> existingAlerts = stockAlertRepository.findDuplicateBatchAlerts(materialBatchId, StockAlert.AlertType.QUARANTINE);
        if (!existingAlerts.isEmpty()) {
            return;
        }

        MaterialBatch materialBatch = materialBatchRepository.findById(materialBatchId)
                .orElseThrow(() -> new DataExistException("Lô vật liệu không tồn tại"));

        // TODO: REFACTOR - MaterialBatch no longer has direct Material reference
        throw new UnsupportedOperationException(
            "Alert creation needs refactoring for new MaterialBatchItem structure");
        
        /*
        StockAlert stockAlert = new StockAlert();
        stockAlert.setMaterial(materialBatch.getMaterial());  // NO LONGER VALID
        stockAlert.setMaterialBatch(materialBatch);
        stockAlert.setAlertType(StockAlert.AlertType.QUARANTINE);
        stockAlert.setAlertMessage(String.format("Lô hàng cần cách ly: %s (Lý do: %s)",
                materialBatch.getBatchNumber(), reason));
        stockAlert.setAlertDate(LocalDateTime.now());
        stockAlert.setIsResolved(false);

        stockAlertRepository.saveAndFlush(stockAlert);
        */
    }

    @Override
    public Long countUnresolvedAlerts() {
        return stockAlertRepository.countUnresolvedAlerts();
    }

    @Override
    public Long countUnresolvedAlertsByType(StockAlert.AlertType alertType) {
        return stockAlertRepository.countUnresolvedAlertsByType(alertType);
    }

    @Override
    public List<StockAlertDto> getAlertsByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        List<StockAlert> stockAlerts = stockAlertRepository.findByAlertDateBetween(fromDate, toDate);
        return stockAlerts.stream()
                .map(stockAlertMapper::toStockAlertDto)
                .collect(java.util.stream.Collectors.toList());
    }
}
