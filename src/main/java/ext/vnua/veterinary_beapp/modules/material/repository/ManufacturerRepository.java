package ext.vnua.veterinary_beapp.modules.material.repository;

import ext.vnua.veterinary_beapp.modules.material.model.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long>, JpaSpecificationExecutor<Manufacturer> {

    Optional<Manufacturer> findByManufacturerCode(String manufacturerCode);

    Optional<Manufacturer> findByManufacturerCodeAndIdNot(String manufacturerCode, Long id);

    List<Manufacturer> findByIsActiveTrue();
}
