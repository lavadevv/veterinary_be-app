package ext.vnua.veterinary_beapp.modules.users.repository;

import ext.vnua.veterinary_beapp.modules.users.model.Position;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long>, JpaSpecificationExecutor<Position> {
    Optional<Position> findByName(String name);

}
