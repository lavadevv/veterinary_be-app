package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.OverheadCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface OverheadCostRepository extends JpaRepository<OverheadCost, Long>, JpaSpecificationExecutor<OverheadCost> {

    // Service dùng: listByDateRange(from, to)
    List<OverheadCost> findByCostDateBetweenOrderByCostDateDesc(LocalDate from, LocalDate to);

    // Service dùng: listByPeriod(periodMonth.withDayOfMonth(1))
    List<OverheadCost> findByPeriodMonthOrderByCostDateDesc(LocalDate periodMonth);

    // Optional tiện ích:
    List<OverheadCost> findByPeriodMonthBetweenOrderByCostDateDesc(LocalDate startMonth, LocalDate endMonth);
}
