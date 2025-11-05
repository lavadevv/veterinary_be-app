package ext.vnua.veterinary_beapp.modules.product.repository;

import ext.vnua.veterinary_beapp.modules.product.model.ProductFormula;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductFormulaRepository extends JpaRepository<ProductFormula, Long>, JpaSpecificationExecutor<ProductFormula> {

    /** Liệt kê các phiên bản theo formulaCode (mới nhất trước) */
    @Query("select f from ProductFormula f join f.header h " +
            "where h.formulaCode = :code " +
            "order by f.createdDate desc, f.id desc")
    @EntityGraph(attributePaths = {"header", "header.products", "formulaItems", "formulaItems.material"})
    Page<ProductFormula> findAllVersions(@Param("code") String formulaCode, Pageable pageable);

    /** Tắt active các phiên bản khác cùng header */
    @Modifying
    @Query("update ProductFormula f set f.isActive = false " +
            "where f.header.id = :headerId and f.id <> :keepId")
    int deactivateOthers(@Param("headerId") Long headerId, @Param("keepId") Long keepId);

    /** Lấy bản mới nhất (có thể active) theo nhiều header */
    @EntityGraph(attributePaths = {"header", "header.products"})
    @Query("select f from ProductFormula f " +
            "where f.header.id in :headerIds " +
            "order by f.createdDate desc, f.id desc")
    List<ProductFormula> findLatestByHeaderIds(@Param("headerIds") List<Long> headerIds);


        /**
          * Lấy phiên bản mới nhất cho mỗi header trong danh sách headerIds
          * và EAGER load cả header + header.products để tránh LazyInitializationException khi map DTO.
          *
          * Ghi chú:
          * - Ở đây dùng "max(f2.id)" để lấy bản mới nhất theo ID. Nếu bạn định nghĩa "mới nhất" theo field "version",
         *   có thể đổi subquery tuỳ logic version của bạn (vd: theo createdDate).
          */
        @EntityGraph(attributePaths = {"header", "header.products"})
        /**
     +     * Lấy phiên bản mới nhất cho mỗi headerId và FETCH luôn:
     +     * - header, header.products
     +     * - formulaItems, formulaItems.material
     +     * Dùng DISTINCT để tránh trùng bản ghi khi join nhiều.
     +     */
        @Query("""
        select distinct f
        from ProductFormula f
        left join fetch f.header h
        left join fetch h.products p
        left join fetch f.formulaItems fi
        left join fetch fi.material m
        where h.id in :headerIds
          and (
            (
              f.isActive = true and
              f.id = (
                select max(fa.id) from ProductFormula fa
                where fa.header.id = h.id and fa.isActive = true
              )
            )
            or (
              not exists (
                select 1 from ProductFormula fx
                where fx.header.id = h.id and fx.isActive = true
              )
              and f.id = (
                select max(f2.id) from ProductFormula f2
                where f2.header.id = h.id
              )
            )
          )
        order by h.id desc
    """)
        List<ProductFormula> findLatestByHeaderIdsWithItemsAndProducts(@Param("headerIds") List<Long> headerIds);

    /**
     * Find active formula by formula code
     * @param formulaCode The formula code to search for
     * @return Optional ProductFormula with eager loaded items and materials
     */
    @Query("""
        select distinct f
        from ProductFormula f
        left join fetch f.header h
        left join fetch h.products p
        left join fetch f.formulaItems fi
        left join fetch fi.material m
        where h.formulaCode = :formulaCode
          and f.isActive = true
        order by f.createdDate desc, f.id desc
    """)
    java.util.Optional<ProductFormula> findByHeaderFormulaCodeAndIsActiveTrue(@Param("formulaCode") String formulaCode);
}
