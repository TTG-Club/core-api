package club.ttg.dnd5.domain.tool.dmscreen.repository;

import club.ttg.dnd5.domain.tool.dmscreen.model.DmScreen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DmScreenRepository extends JpaRepository<DmScreen, UUID> {

    Optional<DmScreen> findByUserId(UUID userId);
}
