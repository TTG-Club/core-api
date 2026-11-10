package club.ttg.dnd5.domain.tool.sheet.repository;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheetEditor;
import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheetEditorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CharacterSheetEditorRepository extends JpaRepository<CharacterSheetEditor, UUID> {

    Optional<CharacterSheetEditor> findBySheetIdAndUserId(UUID sheetId, UUID userId);

    /**
     * Запись для решения владельца. Лист в условии, а не в отдельной проверке: идентификатор записи
     * чужого листа не должен работать даже как подтверждение её существования.
     */
    Optional<CharacterSheetEditor> findByIdAndSheetId(UUID id, UUID sheetId);

    boolean existsBySheetIdAndUserIdAndStatus(UUID sheetId, UUID userId, CharacterSheetEditorStatus status);

    long countBySheetIdAndStatus(UUID sheetId, CharacterSheetEditorStatus status);

    List<CharacterSheetEditor> findAllBySheetIdAndStatusInOrderByCreatedAtAsc(
            UUID sheetId, Collection<CharacterSheetEditorStatus> statuses);

    List<CharacterSheetEditor> findAllBySheetIdInAndStatus(Collection<UUID> sheetIds,
                                                           CharacterSheetEditorStatus status);

    List<CharacterSheetEditor> findAllByUserIdAndSheetIdIn(UUID userId, Collection<UUID> sheetIds);

    /**
     * Неотвеченные запросы на активные листы владельца — для точки у шлема. Удалённые листы не
     * считаются: в истории ответить на запрос нельзя, а точка звала бы туда, где делать нечего.
     */
    @Query("select count(editor) from CharacterSheetEditor editor, CharacterSheet sheet "
            + "where sheet.id = editor.sheetId and sheet.userId = :ownerId and sheet.deleted = false "
            + "and editor.status = club.ttg.dnd5.domain.tool.sheet.model.CharacterSheetEditorStatus.PENDING")
    long countPendingForOwner(@Param("ownerId") UUID ownerId);

    void deleteAllBySheetId(UUID sheetId);

    void deleteAllBySheetIdIn(Collection<UUID> sheetIds);

    void deleteBySheetIdAndUserId(UUID sheetId, UUID userId);
}
