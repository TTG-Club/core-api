package club.ttg.dnd5.domain.tool.sheet.service;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.repository.CharacterSheetRepository;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetListResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetRequest;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.mapper.CharacterSheetMapper;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * Листы персонажей: CRUD с владением по uuid пользователя из JWT, лимитом активных листов
 * и мягким удалением с восстановлением. Содержимое листа — непрозрачный для сервера JSON.
 */
@RequiredArgsConstructor
@Service
public class CharacterSheetService {

    private static final int MAX_ACTIVE_SHEETS = 2;
    private static final int MAX_DELETED_HISTORY_PER_USER = 10;
    private static final String DEFAULT_NAME = "Новый персонаж";

    private final CharacterSheetRepository sheetRepository;
    private final CharacterSheetMapper sheetMapper;

    /**
     * Создаёт лист. Лимит — {@link #getLimitFor(User)} активных листов; документ обязателен.
     */
    @Transactional
    public CharacterSheetResponse create(CharacterSheetRequest request) {
        User user = SecurityUtils.getUser();
        validateLimit(user);
        if (request == null || request.getData() == null || request.getData().isNull()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Не переданы данные листа персонажа (data)");
        }
        CharacterSheet sheet = new CharacterSheet();
        sheet.setUserId(user.getUuid());
        sheet.setName(nameOrDefault(request));
        sheet.setData(request.getData());
        // Флаш сразу: createdAt/updatedAt генерирует БД при INSERT, без него в ответе были бы null.
        return sheetMapper.toResponse(sheetRepository.saveAndFlush(sheet));
    }

    /**
     * Листы текущего пользователя, новые первее, с лимитом и числом активных (для «N из M»
     * на клиенте). {@code includeDeleted=true} — вместе с историей удалённых (без документа).
     */
    public CharacterSheetListResponse findMine(boolean includeDeleted) {
        User user = SecurityUtils.getUser();
        List<CharacterSheet> sheets = includeDeleted
                ? sheetRepository.findAllByUserIdOrderByCreatedAtDesc(user.getUuid())
                : sheetRepository.findAllByUserIdAndDeletedFalseOrderByCreatedAtDesc(user.getUuid());
        long activeCount = sheets.stream().filter(sheet -> !sheet.isDeleted()).count();
        return new CharacterSheetListResponse(
                getLimitFor(user), (int) activeCount, sheetMapper.toListItemResponseList(sheets));
    }

    public CharacterSheetResponse findById(UUID sheetId) {
        return sheetMapper.toResponse(getOwnedActive(sheetId));
    }

    /**
     * Обновление листа: применяются только заполненные поля (название, документ), null — «не менять».
     */
    @Transactional
    public CharacterSheetResponse update(UUID sheetId, CharacterSheetRequest request) {
        CharacterSheet sheet = getOwnedActive(sheetId);
        if (StringUtils.hasText(request.getName())) {
            sheet.setName(request.getName().trim());
        }
        if (request.getData() != null && !request.getData().isNull()) {
            sheet.setData(request.getData());
        }
        return sheetMapper.toResponse(sheet);
    }

    /**
     * Мягкое удаление: лист скрыт из активных, документ сохраняется — восстановление без потерь.
     * История ограничивается последними {@value MAX_DELETED_HISTORY_PER_USER} удалёнными листами —
     * иначе цикл «создать → удалить» рос бы в БД без ограничений: лимит активных удалённые не считает.
     */
    @Transactional
    public void delete(UUID sheetId) {
        CharacterSheet sheet = getOwnedActive(sheetId);
        sheet.setDeleted(true);
        trimDeletedHistory(sheet.getUserId());
    }

    /**
     * Восстановление из истории удалённых. Проверяет лимит активных — вернуть лист сверх
     * лимита нельзя.
     */
    @Transactional
    public CharacterSheetResponse restore(UUID sheetId) {
        User user = SecurityUtils.getUser();
        CharacterSheet sheet = sheetRepository.findById(sheetId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Лист персонажа с id %s не существует", sheetId)));
        requireOwner(sheet, user);
        if (!sheet.isDeleted()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Лист персонажа не удалён");
        }
        validateLimit(user);
        sheet.setDeleted(false);
        return sheetMapper.toResponse(sheet);
    }

    /**
     * Лимит активных листов пользователя. Пока константа; с появлением подписок здесь появится
     * расчёт по уровню подписки пользователя.
     */
    private int getLimitFor(User user) {
        return MAX_ACTIVE_SHEETS;
    }

    private void validateLimit(User user) {
        int limit = getLimitFor(user);
        if (sheetRepository.countByUserIdAndDeletedFalse(user.getUuid()) >= limit) {
            throw new ApiException(HttpStatus.BAD_REQUEST, String.format(
                    "Достигнут лимит листов персонажей: %d. Удалите один из существующих", limit));
        }
    }

    private void trimDeletedHistory(UUID userId) {
        List<CharacterSheet> deleted = sheetRepository.findAllByUserIdAndDeletedTrueOrderByUpdatedAtDesc(userId);
        if (deleted.size() > MAX_DELETED_HISTORY_PER_USER) {
            sheetRepository.deleteAll(deleted.subList(MAX_DELETED_HISTORY_PER_USER, deleted.size()));
        }
    }

    private CharacterSheet getOwnedActive(UUID sheetId) {
        User user = SecurityUtils.getUser();
        CharacterSheet sheet = sheetRepository.findById(sheetId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Лист персонажа с id %s не существует", sheetId)));
        requireOwner(sheet, user);
        return sheet;
    }

    private void requireOwner(CharacterSheet sheet, User user) {
        if (!sheet.getUserId().equals(user.getUuid())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Доступ к листу персонажа запрещен");
        }
    }

    private String nameOrDefault(CharacterSheetRequest request) {
        return StringUtils.hasText(request.getName()) ? request.getName().trim() : DEFAULT_NAME;
    }
}
