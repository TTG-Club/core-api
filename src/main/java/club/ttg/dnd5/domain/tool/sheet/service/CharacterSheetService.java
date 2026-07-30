package club.ttg.dnd5.domain.tool.sheet.service;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.repository.CharacterSheetRepository;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetListResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetPublicResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetRequest;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetShareResponse;
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

    /**
     * Отказ по ссылке «поделиться». Package-private: тем же текстом отвечает
     * {@link SavedCharacterSheetService} — сохранение по битой ссылке и просмотр по ней должны
     * объясняться одинаково.
     */
    static final String SHARED_NOT_FOUND_MESSAGE = "Лист персонажа по этой ссылке не найден";

    private static final String DEFAULT_NAME = "Новый персонаж";

    private final CharacterSheetRepository sheetRepository;
    private final CharacterSheetMapper sheetMapper;
    private final CharacterSheetLimits sheetLimits;

    /**
     * Создаёт лист. Лимит активных листов зависит от подписки ({@link CharacterSheetLimits});
     * документ обязателен.
     */
    @Transactional
    public CharacterSheetResponse create(CharacterSheetRequest request) {
        User user = SecurityUtils.getUser();
        // Тело проверяем до лимита: лимит стоит похода в subscriber-service, а пустой запрос
        // отвергается и без него.
        if (request == null || request.getData() == null || request.getData().isNull()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Не переданы данные листа персонажа (data)");
        }
        validateLimit(user, sheetLimits.forUser(user).activeSheets());
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
     * Глубина истории отдаётся тем же ответом: сколько удалённых листов ещё можно восстановить,
     * клиенту иначе неоткуда узнать.
     * <p>
     * Рядом с выданными лимитами уходят и лимиты подписки: по разнице клиент понимает, стоит ли
     * предлагать её, и не хардкодит числа у себя.
     */
    public CharacterSheetListResponse findMine(boolean includeDeleted) {
        User user = SecurityUtils.getUser();
        SheetLimits limits = sheetLimits.forUser(user);
        SheetLimits subscriberLimits = sheetLimits.subscriberLimits();
        List<CharacterSheet> sheets = includeDeleted
                ? sheetRepository.findAllByUserIdOrderByCreatedAtDesc(user.getUuid())
                : sheetRepository.findAllByUserIdAndDeletedFalseOrderByCreatedAtDesc(user.getUuid());
        long activeCount = sheets.stream().filter(sheet -> !sheet.isDeleted()).count();
        return new CharacterSheetListResponse(limits.activeSheets(), subscriberLimits.activeSheets(),
                limits.deletedHistory(), subscriberLimits.deletedHistory(),
                (int) activeCount, sheetMapper.toListItemResponseList(sheets));
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
     * История ограничивается последними удалёнными листами (глубина зависит от подписки) — иначе
     * цикл «создать → удалить» рос бы в БД без ограничений: лимит активных удалённые не считает.
     */
    @Transactional
    public void delete(UUID sheetId) {
        User user = SecurityUtils.getUser();
        CharacterSheet sheet = getOwnedActive(sheetId);
        sheet.setDeleted(true);
        trimDeletedHistory(user);
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
        validateLimit(user, sheetLimits.forUser(user).activeSheets());
        sheet.setDeleted(false);
        return sheetMapper.toResponse(sheet);
    }

    /**
     * Включает доступ по ссылке и возвращает её токен. Идемпотентно: у уже расшаренного листа
     * токен не перевыпускается, иначе разосланные ранее ссылки молча перестали бы открываться.
     */
    @Transactional
    public CharacterSheetShareResponse share(UUID sheetId) {
        CharacterSheet sheet = getOwnedActive(sheetId);
        if (sheet.getShareToken() == null) {
            sheet.setShareToken(UUID.randomUUID());
        }
        return new CharacterSheetShareResponse(sheet.getShareToken());
    }

    /**
     * Отзывает доступ по ссылке: выданная ранее ссылка перестаёт открываться немедленно
     * и навсегда — повторное «поделиться» выдаст новый токен. Повторный отзыв безопасен.
     */
    @Transactional
    public void revokeShare(UUID sheetId) {
        getOwnedActive(sheetId).setShareToken(null);
    }

    /**
     * Лист по ссылке: чтение без авторизации и без владения. Неизвестный, отозванный или битый
     * токен, как и удалённый лист, — одинаковый 404: наружу не подтверждается даже существование
     * листа. Ручек записи по токену нет — просмотр «только чтение» обеспечен их отсутствием,
     * а не поведением клиента.
     */
    public CharacterSheetPublicResponse findShared(String shareToken) {
        CharacterSheet sheet = sheetRepository.findByShareTokenAndDeletedFalse(parseShareToken(shareToken))
                .orElseThrow(() -> new EntityNotFoundException(SHARED_NOT_FOUND_MESSAGE));
        return sheetMapper.toPublicResponse(sheet);
    }

    /**
     * Токен разбирается вручную, а не конвертером {@code @PathVariable UUID}: ссылку правят руками
     * и обрезают мессенджеры, а мусор в пути должен давать 404, а не 500 от конвертера.
     * <p>
     * Package-private: тем же разбором пользуется {@link SavedCharacterSheetService} — токен
     * туда приходит из тела запроса, но правят и обрезают его так же.
     */
    static UUID parseShareToken(String shareToken) {
        if (!StringUtils.hasText(shareToken)) {
            throw new EntityNotFoundException(SHARED_NOT_FOUND_MESSAGE);
        }
        try {
            return UUID.fromString(shareToken.trim());
        } catch (IllegalArgumentException e) {
            throw new EntityNotFoundException(SHARED_NOT_FOUND_MESSAGE);
        }
    }

    private void validateLimit(User user, int limit) {
        if (sheetRepository.countByUserIdAndDeletedFalse(user.getUuid()) >= limit) {
            throw new ApiException(HttpStatus.BAD_REQUEST, String.format(
                    "Достигнут лимит листов персонажей: %d. Удалите один из существующих", limit));
        }
    }

    /**
     * Вытесняет из истории самые старые удалённые листы. Подрезаем по
     * {@link SheetLimits#deletedHistoryToTrim()}, а не по показанной клиенту глубине: удаление
     * здесь физическое и необратимое, поэтому при неизвестном статусе подписки история сохраняется
     * по максимуму.
     */
    private void trimDeletedHistory(User user) {
        int keep = sheetLimits.forUser(user).deletedHistoryToTrim();
        List<CharacterSheet> deleted = sheetRepository
                .findAllByUserIdAndDeletedTrueOrderByUpdatedAtDesc(user.getUuid());
        if (deleted.size() > keep) {
            sheetRepository.deleteAll(deleted.subList(keep, deleted.size()));
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
