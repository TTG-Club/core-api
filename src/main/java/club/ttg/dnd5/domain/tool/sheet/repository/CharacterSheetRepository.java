package club.ttg.dnd5.domain.tool.sheet.repository;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CharacterSheetRepository extends JpaRepository<CharacterSheet, UUID> {

    long countByUserIdAndDeletedFalse(UUID userId);

    List<CharacterSheet> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    List<CharacterSheet> findAllByUserIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId);

    List<CharacterSheet> findAllByUserIdAndDeletedTrueOrderByUpdatedAtDesc(UUID userId);
}
