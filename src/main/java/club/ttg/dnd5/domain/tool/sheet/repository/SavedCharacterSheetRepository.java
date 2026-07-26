package club.ttg.dnd5.domain.tool.sheet.repository;

import club.ttg.dnd5.domain.tool.sheet.model.SavedCharacterSheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedCharacterSheetRepository extends JpaRepository<SavedCharacterSheet, UUID> {

    long countByUserId(UUID userId);

    List<SavedCharacterSheet> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Уже сохранённый лист: повторное сохранение по новой ссылке обновляет запись.
     */
    Optional<SavedCharacterSheet> findByUserIdAndSheetId(UUID userId, UUID sheetId);

    /**
     * Запись для удаления. Владелец записи в условии, а не в отдельной проверке: чужую
     * сохранённую ссылку не должно быть видно даже по факту существования.
     */
    Optional<SavedCharacterSheet> findByIdAndUserId(UUID id, UUID userId);
}
