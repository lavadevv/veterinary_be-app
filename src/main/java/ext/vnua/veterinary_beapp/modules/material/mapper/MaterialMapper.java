package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.material.CreateMaterialRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.material.UpdateMaterialRequest;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(
        componentModel = "spring",
        uses = {SupplierMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MaterialMapper {

    @Mapping(source = "supplier", target = "supplierDto")
    MaterialDto toMaterialDto(Material material);

    @Mapping(source = "supplierDto", target = "supplier")
    Material toMaterial(MaterialDto materialDto);

    // Create: bỏ id/supplier/batches, isActive sẽ set mặc định ở service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "batches", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Material toCreateMaterial(CreateMaterialRequest request);

    // Map từ Update DTO sang entity MỚI (ít dùng); giữ lại để tương thích, nhưng khuyến nghị dùng updateMaterialFromRequest
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "batches", ignore = true)
    Material toUpdateMaterial(UpdateMaterialRequest request);

    // Update in-place (PATCH/PUT): bỏ id/supplier/batches, IGNORE null để không overwrite
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "batches", ignore = true)
    void updateMaterialFromRequest(UpdateMaterialRequest request, @MappingTarget Material material);

    @AfterMapping
    default void afterMappingCreate(@MappingTarget Material material, CreateMaterialRequest request) {
        if (material.getCurrentStock() == null) {
            material.setCurrentStock(BigDecimal.ZERO);
        }
        if (material.getIsActive() == null) {
            material.setIsActive(true);
        }
        if (material.getRequiresColdStorage() == null) {
            material.setRequiresColdStorage(false);
        }
    }

    @AfterMapping
    default void afterMappingUpdate(@MappingTarget Material material, UpdateMaterialRequest request) {
        // Chỉ đảm bảo không null; không ép isActive nếu DTO không gửi (đã IGNORE null ở trên)
        if (material.getCurrentStock() == null) {
            material.setCurrentStock(BigDecimal.ZERO);
        }
        if (material.getRequiresColdStorage() == null) {
            material.setRequiresColdStorage(false);
        }
    }
}
