package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.MaterialPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MaterialPriceHistoryRepository extends JpaRepository<MaterialPriceHistory, Long> {
    List<MaterialPriceHistory> findByMaterialIdOrderByEffectiveDateDesc(Long materialId);

    Optional<MaterialPriceHistory> findTopByMaterialIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
            Long materialId, LocalDate effectiveDate);
}