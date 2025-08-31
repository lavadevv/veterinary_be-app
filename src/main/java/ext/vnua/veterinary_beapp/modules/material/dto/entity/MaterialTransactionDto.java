package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import ext.vnua.veterinary_beapp.modules.material.model.MaterialTransaction.TransactionType;
import ext.vnua.veterinary_beapp.modules.users.dto.entity.UserDto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MaterialTransactionDto {
    private Long id;
    private MaterialBatchDto materialBatchDto;
    private TransactionType transactionType;
    private LocalDateTime transactionDate;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalValue;
    private String referenceDocument;
    private String productionOrderId;
    private LocationDto fromLocationDto;
    private LocationDto toLocationDto;
    private String reason;
    private String notes;
    private UserDto createdByDto;
    private UserDto approvedByDto;
}