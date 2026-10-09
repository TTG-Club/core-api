package club.ttg.dnd5.domain.tool.sheet.service;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheetEditor;
import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheetEditorStatus;
import club.ttg.dnd5.domain.tool.sheet.repository.CharacterSheetEditorRepository;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Листы персонажей: CRUD с владением по uuid пользователя из JWT, лимитом активных листов
 * и мягким удалением с восстановлением. Содержимое листа — непрозрачный для сервера JSON.
 * <p>
 * Открыть и сохранить лист может и редактор, которому владелец дал право
 * ({@link CharacterSheetEditorService}); удаление, восстановление и ссылка — только у владельца.
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

    private static final String VERSION_CONFLICT_MESSAGE =
            "Лист персонажа уже изменили в другом месте — загрузите актуальную версию";

    private final CharacterSheetRepository sheetRepository;
    private final CharacterSheetEditorRepository editorRepository;
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
        List<CharacterSheetResponse> responses = sheetMapper.toListItemResponseList(sheets);
        Map<UUID, Long> pendingRequests = countPendingEditRequests(sheets);
        responses.forEach(response -> response.setPendingEditRequests(
                pendingRequests.getOrDefault(response.getId(), 0L).intValue()));
        return new CharacterSheetListResponse(limits.activeSheets(), subscriberLimits.activeSheets(),
                limits.deletedHistory(), subscriberLimits.deletedHistory(),
                (int) activeCount, responses);
    }

    /**
     * Лист целиком — владельцу или редактору, которому владелец дал право.
     */
    public CharacterSheetResponse findById(UUID sheetId) {
        User user = SecurityUtils.getUser();
        return toAccessibleResponse(getEditableActive(sheetId, user), user);
    }

    /**
     * Обновление листа: применяются только заполненные поля (название, документ), null — «не менять».
     * <p>
     * Лист пишется целиком, поэтому присланная версия сверяется с текущей: устаревшая — 409, иначе
     * сохранение из одной вкладки молча затёрло бы правку из другой (или хиты, записанные мастером
     * боя). Без версии проверки нет — так пишут клиенты, ещё не знающие о ней.
     */
    @Transactional
    public CharacterSheetResponse update(UUID sheetId, CharacterSheetRequest request) {
        User user = SecurityUtils.getUser();
        CharacterSheet sheet = getEditableActive(sheetId, user);
        if (request.getVersion() != null && request.getVersion() != sheet.getVersion()) {
            throw new ApiException(HttpStatus.CONFLICT, VERSION_CONFLICT_MESSAGE);
        }
        if (StringUtils.hasText(request.getName())) {
            sheet.setName(request.getName().trim());
        }
        if (request.getData() != null && !request.getData().isNull()) {
            sheet.setData(request.getData());
        }
        // Флаш сразу: версию увеличивает Hibernate при UPDATE, без него в ответе была бы прежняя,
        // и следующее сохранение клиента получило бы ложный конфликт.
        return toAccessibleResponse(sheetRepository.saveAndFlush(sheet), user);
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
     * <p>
     * Вместе со ссылкой снимаются права редакторов и неотвеченные запросы: у владельца один
     * рубильник «закрыть всё», и редактор не остаётся в листе, который владелец считает закрытым.
     */
    @Transactional
    public void revokeShare(UUID sheetId) {
        CharacterSheet sheet = getOwnedActive(sheetId);
        sheet.setShareToken(null);
        editorRepository.deleteAllBySheetId(sheet.getId());
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
     * Любой активный лист — для администратора: так он открывает лист из баг-репорта, даже если
     * владелец не делился ссылкой. Владение не проверяется, роль проверяет контроллер. Ответ тот же,
     * что по ссылке, — без токена ссылки владельца. Ручек записи в чужой лист нет: просмотр
     * «только чтение» обеспечен их отсутствием, а не поведением клиента.
     */
    public CharacterSheetPublicResponse findForAdmin(UUID sheetId) {
        return sheetMapper.toPublicResponse(getActive(sheetId));
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
            List<CharacterSheet> evicted = deleted.subList(keep, deleted.size());
            editorRepository.deleteAllBySheetIdIn(evicted.stream().map(CharacterSheet::getId).toList());
            sheetRepository.deleteAll(evicted);
        }
    }

    private CharacterSheet getOwnedActive(UUID sheetId) {
        User user = SecurityUtils.getUser();
        CharacterSheet sheet = getActive(sheetId);
        requireOwner(sheet, user);
        return sheet;
    }

    /**
     * Активный лист, который пользователь может открыть и сохранить: свой или тот, на который
     * владелец дал ему право. Остальное — 403, как и раньше для чужого листа: загрузчик клиента по
     * этому ответу решает, перечитывать ли лист администраторской ручкой.
     */
    private CharacterSheet getEditableActive(UUID sheetId, User user) {
        CharacterSheet sheet = getActive(sheetId);
        if (!isOwner(sheet, user) && !isApprovedEditor(sheet, user)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Доступ к листу персонажа запрещен");
        }
        return sheet;
    }

    /**
     * Ответ с листом с учётом того, кто его открыл. Редактору токен ссылки не отдаётся: управлять
     * доступом может только владелец, а по токену редактор раздал бы лист дальше.
     */
    private CharacterSheetResponse toAccessibleResponse(CharacterSheet sheet, User user) {
        CharacterSheetResponse response = sheetMapper.toResponse(sheet);
        if (!isOwner(sheet, user)) {
            response.setShareToken(null);
            response.setEditor(true);
        }
        return response;
    }

    private boolean isApprovedEditor(CharacterSheet sheet, User user) {
        return editorRepository.existsBySheetIdAndUserIdAndStatus(
                sheet.getId(), user.getUuid(), CharacterSheetEditorStatus.APPROVED);
    }

    /**
     * Неотвеченные запросы по активным листам одним запросом: метка на карточке нужна списку сразу.
     */
    private Map<UUID, Long> countPendingEditRequests(List<CharacterSheet> sheets) {
        List<UUID> activeIds = sheets.stream()
                .filter(sheet -> !sheet.isDeleted())
                .map(CharacterSheet::getId)
                .toList();
        if (activeIds.isEmpty()) {
            return Map.of();
        }
        return editorRepository.findAllBySheetIdInAndStatus(activeIds, CharacterSheetEditorStatus.PENDING)
                .stream()
                .collect(Collectors.groupingBy(CharacterSheetEditor::getSheetId, Collectors.counting()));
    }

    private CharacterSheet getActive(UUID sheetId) {
        return sheetRepository.findById(sheetId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Лист персонажа с id %s не существует", sheetId)));
    }

    private boolean isOwner(CharacterSheet sheet, User user) {
        return sheet.getUserId().equals(user.getUuid());
    }

    private void requireOwner(CharacterSheet sheet, User user) {
        if (!isOwner(sheet, user)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Доступ к листу персонажа запрещен");
        }
    }

    private String nameOrDefault(CharacterSheetRequest request) {
        return StringUtils.hasText(request.getName()) ? request.getName().trim() : DEFAULT_NAME;
    }
}
