package ext.vnua.veterinary_beapp.modules.production.repository;

import ext.vnua.veterinary_beapp.modules.production.model.ProductionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductionLineRepository
        extends JpaRepository<ProductionLine, Long>, JpaSpecificationExecutor<ProductionLine> {

    Optional<ProductionLine> findByLineCode(String lineCode);

    boolean existsByLineCode(String lineCode);

    List<ProductionLine> findByStatus(String status);

    List<ProductionLine> findByStatusOrderByNameAsc(String status);

    @Query("SELECT COUNT(pl) FROM ProductionLine pl WHERE pl.status = :status")
    long countByStatus(String status);

    // Tìm dây chuyền theo name (case insensitive)
    @Query("SELECT pl FROM ProductionLine pl WHERE LOWER(pl.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<ProductionLine> findByNameContainingIgnoreCase(String name);
}