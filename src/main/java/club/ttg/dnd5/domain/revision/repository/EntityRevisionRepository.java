package club.ttg.dnd5.domain.revision.repository;

import club.ttg.dnd5.domain.revision.model.EntityRevision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EntityRevisionRepository extends JpaRepository<EntityRevision, Long> {

    List<EntityRevision> findByEntityTypeAndEntityIdOrderByVersionDesc(String entityType, String entityId);

    Optional<EntityRevision> findFirstByEntityTypeAndEntityIdOrderByVersionDesc(String entityType, String entityId);

    Optional<EntityRevision> findByEntityTypeAndEntityIdAndVersion(String entityType, String entityId, int version);
}
