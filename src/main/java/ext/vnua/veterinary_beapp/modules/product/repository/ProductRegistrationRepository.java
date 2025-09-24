package ext.vnua.veterinary_beapp.modules.product.repository;

import ext.vnua.veterinary_beapp.modules.product.model.ProductRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRegistrationRepository extends JpaRepository<ProductRegistration, Long>, JpaSpecificationExecutor<ProductRegistration> {
    Optional<ProductRegistration> findByRegistrationNumber(String registrationNumber);
    Optional<ProductRegistration> findByProductId(Long productId);
}