package ext.vnua.veterinary_beapp.modules.production.repository;

import ext.vnua.veterinary_beapp.modules.production.model.ProductionBatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductionBatchRecordRepository
        extends JpaRepository<ProductionBatchRecord, Long>, JpaSpecificationExecutor<ProductionBatchRecord> {

    boolean existsByProductionOrderIdAndStepName(Long productionOrderId, String stepName);

    @Query("SELECT MAX(r.sequenceNumber) FROM ProductionBatchRecord r WHERE r.productionOrder.id = :orderId AND r.stepName = :stepName")
    Integer findMaxSequenceNumberByProductionOrderIdAndStepName(@Param("orderId") Long orderId, @Param("stepName") String stepName);
}
