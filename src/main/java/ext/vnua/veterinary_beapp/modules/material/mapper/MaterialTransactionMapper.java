package ext.vnua.veterinary_beapp.modules.material.mapper;

import ext.vnua.veterinary_beapp.modules.material.dto.entity.MaterialTransactionDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialTransaction.CreateMaterialTransactionRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.materialTransaction.UpdateMaterialTransactionRequest;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialTransaction;

import ext.vnua.veterinary_beapp.modules.users.mapper.UserMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {MaterialBatchMapper.class, LocationMapper.class, UserMapper.class})
public interface MaterialTransactionMapper {
    MaterialTransactionMapper INSTANCE = Mappers.getMapper(MaterialTransactionMapper.class);

    @Mapping(source = "materialBatch", target = "materialBatchDto")
    @Mapping(source = "fromLocation", target = "fromLocationDto")
    @Mapping(source = "toLocation", target = "toLocationDto")
    @Mapping(source = "approvedBy", target = "approvedByDto")
    MaterialTransactionDto toMaterialTransactionDto(MaterialTransaction materialTransaction);

    @Mapping(source = "materialBatchDto", target = "materialBatch")
    @Mapping(source = "fromLocationDto", target = "fromLocation")
    @Mapping(source = "toLocationDto", target = "toLocation")
    @Mapping(source = "approvedByDto", target = "approvedBy")
    MaterialTransaction toMaterialTransaction(MaterialTransactionDto materialTransactionDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "materialBatch", ignore = true)
    @Mapping(target = "fromLocation", ignore = true)
    @Mapping(target = "toLocation", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    MaterialTransaction toCreateMaterialTransaction(CreateMaterialTransactionRequest request);

    @Mapping(target = "materialBatch", ignore = true)
    @Mapping(target = "fromLocation", ignore = true)
    @Mapping(target = "toLocation", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    MaterialTransaction toUpdateMaterialTransaction(UpdateMaterialTransactionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "materialBatch", ignore = true)
    @Mapping(target = "fromLocation", ignore = true)
    @Mapping(target = "toLocation", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    void updateMaterialTransactionFromRequest(UpdateMaterialTransactionRequest request, @MappingTarget MaterialTransaction materialTransaction);

    @AfterMapping
    default void afterMappingCreate(@MappingTarget MaterialTransaction materialTransaction, CreateMaterialTransactionRequest request) {
        // Calculate total value if not provided
        if (materialTransaction.getTotalValue() == null &&
                materialTransaction.getUnitPrice() != null &&
                materialTransaction.getQuantity() != null) {
            materialTransaction.setTotalValue(materialTransaction.getUnitPrice().multiply(materialTransaction.getQuantity()));
        }
    }

    @AfterMapping
    default void afterMappingUpdate(@MappingTarget MaterialTransaction materialTransaction, UpdateMaterialTransactionRequest request) {
        // Calculate total value if not provided
        if (materialTransaction.getTotalValue() == null &&
                materialTransaction.getUnitPrice() != null &&
                materialTransaction.getQuantity() != null) {
            materialTransaction.setTotalValue(materialTransaction.getUnitPrice().multiply(materialTransaction.getQuantity()));
        }
    }
}
