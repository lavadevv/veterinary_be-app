package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.Supplier;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {

    @EntityGraph(attributePaths = {"materials"})
    Optional<Supplier> findById(Long id);

    // Tìm theo mã nhà cung cấp
    Optional<Supplier> findBySupplierCode(String supplierCode);

    // Kiểm tra mã nhà cung cấp trùng (loại trừ ID hiện tại - dùng cho update)
    Optional<Supplier> findBySupplierCodeAndIdNot(String supplierCode, Long id);

    // Tìm theo tên nhà cung cấp
    List<Supplier> findBySupplierNameContainingIgnoreCase(String supplierName);

    // Tìm nhà cung cấp đang hoạt động
    List<Supplier> findByIsActiveTrue();

    // Tìm theo quốc gia xuất xứ
    List<Supplier> findByCountryOfOrigin(String countryOfOrigin);

    // Tìm theo email
    Optional<Supplier> findByEmail(String email);

    // Tìm theo số điện thoại
    Optional<Supplier> findByPhone(String phone);

    // Tìm nhà cung cấp có chứng chỉ GMP sắp hết hạn
    @Query("SELECT s FROM Supplier s " +
            "WHERE s.gmpExpiryDate BETWEEN :startDate AND :endDate " +
            "AND s.isActive = true")
    List<Supplier> findSuppliersWithExpiringGmp(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    // Tìm nhà cung cấp có chứng chỉ GMP đã hết hạn
    @Query("SELECT s FROM Supplier s " +
            "WHERE s.gmpExpiryDate < :currentDate " +
            "AND s.isActive = true")
    List<Supplier> findSuppliersWithExpiredGmp(@Param("currentDate") LocalDate currentDate);
}
