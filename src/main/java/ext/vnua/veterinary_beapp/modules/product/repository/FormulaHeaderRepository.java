// File: ext/vnua/veterinary_beapp/modules/product/repository/FormulaHeaderRepository.java
package ext.vnua.veterinary_beapp.modules.product.repository;

import ext.vnua.veterinary_beapp.modules.product.model.FormulaHeader;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface FormulaHeaderRepository extends JpaRepository<FormulaHeader, Long> {

    Optional<FormulaHeader> findByFormulaCode(String code);

    @Query("""
       select distinct h from FormulaHeader h
       left join h.products p
       where (:q is null or lower(h.formulaCode) like lower(concat('%',:q,'%'))
                      or lower(h.formulaName) like lower(concat('%',:q,'%')) )
         and (:productId is null or p.id = :productId)
    """)
    Page<FormulaHeader> searchHeaders(@Param("q") String q,
                                      @Param("productId") Long productId,
                                      Pageable pageable);
}
