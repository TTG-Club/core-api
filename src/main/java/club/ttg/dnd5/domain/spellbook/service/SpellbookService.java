package club.ttg.dnd5.domain.spellbook.service;

import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.mapper.SpellMapper;
import club.ttg.dnd5.domain.spellbook.model.Spellbook;
import club.ttg.dnd5.domain.spellbook.model.SpellbookAccess;
import club.ttg.dnd5.domain.spellbook.model.SpellbookSpell;
import club.ttg.dnd5.domain.spellbook.repository.SpellbookAccessRepository;
import club.ttg.dnd5.domain.spellbook.repository.SpellbookRepository;
import club.ttg.dnd5.domain.spellbook.repository.SpellbookSpellCount;
import club.ttg.dnd5.domain.spellbook.repository.SpellbookSpellRepository;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookAddSpellsRequest;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookDetailedResponse;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookLevelGroupResponse;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookListResponse;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookRequest;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookShortResponse;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookSpellResponse;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookSpellUpdateRequest;
import club.ttg.dnd5.domain.spellbook.rest.mapper.SpellbookMapper;
import club.ttg.dnd5.domain.subscription.service.SubscriptionStatusClient;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Книги заклинаний пользователя: личные наборы заклинаний с отметками подготовленных.
 * <p>
 * Работать с книгами может только зарегистрированный пользователь. Менять книгу может лишь
 * владелец; остальным она доступна на чтение, если они добавили её себе по ссылке
 * ({@link Spellbook#getShareKey()}). Без активной подписки можно хранить не больше
 * {@value MAX_FREE_SPELLBOOKS} своих книг, с подпиской — без ограничения; книги, доступные
 * по ссылке, в лимит не входят.
 */
@RequiredArgsConstructor
@Service
public class SpellbookService {

    /** Лимит книг без активной подписки. */
    public static final int MAX_FREE_SPELLBOOKS = 3;

    private static final String DEFAULT_NAME = "Новая книга заклинаний";

    private final SpellbookRepository spellbookRepository;
    private final SpellbookSpellRepository spellbookSpellRepository;
    private final SpellbookAccessRepository accessRepository;
    private final SpellRepository spellRepository;
    private final SubscriptionStatusClient subscriptionStatusClient;
    private final SpellbookMapper spellbookMapper;
    private final SpellMapper spellMapper;

    /**
     * Создаёт книгу текущего пользователя, при необходимости сразу наполняя её заклинаниями.
     * Без активной подписки больше {@value MAX_FREE_SPELLBOOKS} книг создать нельзя.
     */
    @Transactional
    public SpellbookDetailedResponse create(SpellbookRequest request) {
        String username = currentUsername();
        validateSpellbookLimit(username);

        Spellbook spellbook = new Spellbook();
        spellbook.setName(nameOrDefault(request));
        spellbook.setOwnerUsername(username);
        spellbook.setShareKey(UUID.randomUUID());

        // Флаш сразу: createdAt/updatedAt генерирует БД при INSERT, без него в ответе были бы null.
        Spellbook saved = spellbookRepository.saveAndFlush(spellbook);
        if (request != null && !isEmpty(request.getSpells())) {
            addSpells(saved.getId(), request.getSpells(), false);
        }
        return toOwnResponse(saved);
    }

    /**
     * Книги пользователя двумя списками: свои (новые первее) и доступные по ссылке
     * (последние добавленные первее).
     */
    @Transactional(readOnly = true)
    public SpellbookListResponse findMine() {
        String username = currentUsername();
        List<Spellbook> own = spellbookRepository.findAllByOwnerUsernameOrderByCreatedAtDesc(username);
        List<Spellbook> shared = findShared(username);

        Map<UUID, SpellbookSpellCount> counts = counts(Stream.concat(own.stream(), shared.stream())
                .map(Spellbook::getId)
                .toList());

        return new SpellbookListResponse(
                own.stream()
                        .map(spellbook -> toShortResponse(spellbook, counts, true))
                        .toList(),
                shared.stream()
                        .map(spellbook -> toShortResponse(spellbook, counts, false))
                        .toList());
    }

    /** Книга по id: своя — на чтение и изменение, добавленная по ссылке — только на чтение. */
    @Transactional(readOnly = true)
    public SpellbookDetailedResponse findById(UUID spellbookId) {
        String username = currentUsername();
        Spellbook spellbook = spellbookRepository.findById(spellbookId)
                .filter(found -> username.equals(found.getOwnerUsername())
                        || accessRepository.existsBySpellbookIdAndUserUsername(spellbookId, username))
                .orElseThrow(() -> notFound(spellbookId));
        return username.equals(spellbook.getOwnerUsername())
                ? toOwnResponse(spellbook)
                : toSharedResponse(spellbook);
    }

    /**
     * Открывает чужую книгу по ссылке, не добавляя её в список доступных — предпросмотр
     * перед добавлением. Владельцу отдаётся его собственная книга.
     */
    @Transactional(readOnly = true)
    public SpellbookDetailedResponse findByShareKey(UUID shareKey) {
        String username = currentUsername();
        Spellbook spellbook = getByShareKey(shareKey);
        return username.equals(spellbook.getOwnerUsername())
                ? toOwnResponse(spellbook)
                : toSharedResponse(spellbook);
    }

    /**
     * Добавляет чужую книгу в список доступных по ссылке. Повторное добавление и переход
     * владельца по своей же ссылке ничего не меняют.
     */
    @Transactional
    public SpellbookDetailedResponse addShared(UUID shareKey) {
        String username = currentUsername();
        Spellbook spellbook = getByShareKey(shareKey);
        if (username.equals(spellbook.getOwnerUsername())) {
            return toOwnResponse(spellbook);
        }
        if (!accessRepository.existsBySpellbookIdAndUserUsername(spellbook.getId(), username)) {
            SpellbookAccess access = new SpellbookAccess();
            access.setSpellbookId(spellbook.getId());
            access.setUserUsername(username);
            accessRepository.save(access);
        }
        return toSharedResponse(spellbook);
    }

    /**
     * Убирает доступную по ссылке книгу из отображения пользователя. Сама книга и её
     * доступ у других пользователей не затрагиваются — вернуть её можно по той же ссылке.
     */
    @Transactional
    public void deleteShared(UUID spellbookId) {
        String username = currentUsername();
        accessRepository.findBySpellbookIdAndUserUsername(spellbookId, username)
                .orElseThrow(() -> notFound(spellbookId));
        accessRepository.deleteBySpellbookIdAndUserUsername(spellbookId, username);
    }

    /** Переименование книги: пустое имя — не менять. */
    @Transactional
    public SpellbookDetailedResponse update(UUID spellbookId, SpellbookRequest request) {
        Spellbook spellbook = getOwned(spellbookId);
        if (StringUtils.hasText(request.getName())) {
            spellbook.setName(request.getName().trim());
        }
        return toOwnResponse(spellbook);
    }

    /** Удаляет книгу владельца: вместе с заклинаниями и доступами, выданными по ссылке. */
    @Transactional
    public void delete(UUID spellbookId) {
        Spellbook spellbook = getOwned(spellbookId);
        spellbookSpellRepository.deleteAllBySpellbookId(spellbook.getId());
        accessRepository.deleteAllBySpellbookId(spellbook.getId());
        spellbookRepository.delete(spellbook);
    }

    /** Добавляет заклинания в книгу; уже добавленные пропускаются. */
    @Transactional
    public SpellbookDetailedResponse addSpells(UUID spellbookId, SpellbookAddSpellsRequest request) {
        Spellbook spellbook = getOwned(spellbookId);
        addSpells(spellbook.getId(), request.getSpells(), Boolean.TRUE.equals(request.getPrepared()));
        touch(spellbook);
        return toOwnResponse(spellbook);
    }

    /** Отметка «подготовлено» у заклинания книги. */
    @Transactional
    public SpellbookDetailedResponse updateSpell(UUID spellbookId,
                                                 String spellUrl,
                                                 SpellbookSpellUpdateRequest request) {
        Spellbook spellbook = getOwned(spellbookId);
        SpellbookSpell entry = spellbookSpellRepository
                .findBySpellbookIdAndSpellUrl(spellbook.getId(), spellUrl)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Заклинание %s не добавлено в книгу", spellUrl)));
        entry.setPrepared(request.getPrepared());
        touch(spellbook);
        return toOwnResponse(spellbook);
    }

    @Transactional
    public SpellbookDetailedResponse deleteSpell(UUID spellbookId, String spellUrl) {
        Spellbook spellbook = getOwned(spellbookId);
        spellbookSpellRepository.findBySpellbookIdAndSpellUrl(spellbook.getId(), spellUrl)
                .ifPresent(spellbookSpellRepository::delete);
        touch(spellbook);
        return toOwnResponse(spellbook);
    }

    /**
     * Сохраняет новые заклинания книги: несуществующие и скрытые отклоняются, уже добавленные
     * пропускаются (повторное добавление из раздела заклинаний не должно падать ошибкой).
     */
    private void addSpells(UUID spellbookId, Set<String> spellUrls, boolean prepared) {
        Set<String> requested = spellUrls.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (requested.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Укажите заклинания для добавления");
        }

        Set<String> existing = spellRepository.findVisibleUrls(requested);
        if (existing.size() < requested.size()) {
            String missing = requested.stream()
                    .filter(url -> !existing.contains(url))
                    .collect(Collectors.joining(", "));
            throw new EntityNotFoundException(String.format("Заклинания не существуют: %s", missing));
        }

        Set<String> already = spellbookSpellRepository.findSpellUrlsBySpellbookId(spellbookId);
        List<SpellbookSpell> entries = requested.stream()
                .filter(url -> !already.contains(url))
                .map(url -> {
                    SpellbookSpell entry = new SpellbookSpell();
                    entry.setSpellbookId(spellbookId);
                    entry.setSpellUrl(url);
                    entry.setPrepared(prepared);
                    return entry;
                })
                .toList();
        spellbookSpellRepository.saveAll(entries);
    }

    /**
     * Лимит книг без подписки. Действующая подписка снимает ограничение; статус берётся
     * из subscriber-service по факту подписки, а не по роли {@code SUBSCRIBER} из токена —
     * роль живёт в JWT до перелогина и пережила бы окончание подписки.
     * <p>
     * Fail-closed: при недоступности subscriber-service подписки считается нет, и
     * подписчик упрётся в лимит бесплатных книг. Уже созданные книги при этом остаются
     * доступны — блокируется только создание новых.
     */
    private void validateSpellbookLimit(String username) {
        if (subscriptionStatusClient.fetch(username).active()) {
            return;
        }
        if (spellbookRepository.countByOwnerUsername(username) >= MAX_FREE_SPELLBOOKS) {
            throw new ApiException(HttpStatus.FORBIDDEN, String.format(
                    "Без подписки можно сохранить не больше %d книг заклинаний. "
                            + "Удалите одну из существующих или оформите подписку", MAX_FREE_SPELLBOOKS));
        }
    }

    /**
     * Книга по id с проверкой владельца — для всех изменяющих операций: книга, доступная
     * по ссылке, редактируется только владельцем и здесь неотличима от несуществующей.
     */
    private Spellbook getOwned(UUID spellbookId) {
        return spellbookRepository.findByIdAndOwnerUsername(spellbookId, currentUsername())
                .orElseThrow(() -> notFound(spellbookId));
    }

    private Spellbook getByShareKey(UUID shareKey) {
        return spellbookRepository.findByShareKey(shareKey)
                .orElseThrow(() -> new EntityNotFoundException("Книга заклинаний по ссылке не найдена"));
    }

    private static EntityNotFoundException notFound(UUID spellbookId) {
        return new EntityNotFoundException(
                String.format("Книга заклинаний с id %s не существует", spellbookId));
    }

    private SpellbookDetailedResponse toOwnResponse(Spellbook spellbook) {
        List<SpellbookLevelGroupResponse> levels = levels(spellbook);
        return spellbookMapper.toOwnDetailedResponse(spellbook, levels,
                spellCount(levels), preparedCount(levels));
    }

    private SpellbookDetailedResponse toSharedResponse(Spellbook spellbook) {
        List<SpellbookLevelGroupResponse> levels = levels(spellbook);
        return spellbookMapper.toSharedDetailedResponse(spellbook, levels,
                spellCount(levels), preparedCount(levels));
    }

    private SpellbookShortResponse toShortResponse(Spellbook spellbook,
                                                   Map<UUID, SpellbookSpellCount> counts,
                                                   boolean own) {
        SpellbookSpellCount count = counts.get(spellbook.getId());
        long total = count == null ? 0 : count.getTotal();
        long prepared = count == null ? 0 : count.getPrepared();
        return own
                ? spellbookMapper.toOwnShortResponse(spellbook, total, prepared)
                : spellbookMapper.toSharedShortResponse(spellbook, total, prepared);
    }

    /**
     * Книги, добавленные пользователем по ссылке, в порядке выдачи доступа (последние первее).
     * Порядок восстанавливается по списку доступов: {@code findAllById} его не сохраняет.
     */
    private List<Spellbook> findShared(String username) {
        List<UUID> spellbookIds = accessRepository.findAllByUserUsernameOrderByCreatedAtDesc(username).stream()
                .map(SpellbookAccess::getSpellbookId)
                .toList();
        if (spellbookIds.isEmpty()) {
            return List.of();
        }
        Map<UUID, Spellbook> byId = spellbookRepository.findAllById(spellbookIds).stream()
                .collect(Collectors.toMap(Spellbook::getId, Function.identity()));
        return spellbookIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<UUID, SpellbookSpellCount> counts(List<UUID> spellbookIds) {
        if (spellbookIds.isEmpty()) {
            return Map.of();
        }
        return spellbookSpellRepository.countsBySpellbookIds(spellbookIds).stream()
                .collect(Collectors.toMap(SpellbookSpellCount::getSpellbookId, Function.identity()));
    }

    private List<SpellbookLevelGroupResponse> levels(Spellbook spellbook) {
        return buildLevels(spellbookSpellRepository.findAllBySpellbookId(spellbook.getId()));
    }

    private static long spellCount(List<SpellbookLevelGroupResponse> levels) {
        return levels.stream().mapToLong(SpellbookLevelGroupResponse::getSpellCount).sum();
    }

    private static long preparedCount(List<SpellbookLevelGroupResponse> levels) {
        return levels.stream().mapToLong(SpellbookLevelGroupResponse::getPreparedCount).sum();
    }

    /**
     * Разбивает заклинания книги на группы по уровню (заговоры первыми). Внутри группы порядок
     * по названию — его задаёт запрос заклинаний.
     */
    private List<SpellbookLevelGroupResponse> buildLevels(List<SpellbookSpell> entries) {
        if (entries.isEmpty()) {
            return List.of();
        }
        Map<String, Boolean> preparedByUrl = entries.stream()
                .collect(Collectors.toMap(SpellbookSpell::getSpellUrl, SpellbookSpell::isPrepared));

        Map<Long, List<SpellbookSpellResponse>> byLevel = new TreeMap<>();
        for (Spell spell : spellRepository.findAllShortByUrls(preparedByUrl.keySet())) {
            byLevel.computeIfAbsent(spell.getLevel(), level -> new ArrayList<>())
                    .add(new SpellbookSpellResponse(spellMapper.toShort(spell),
                            Boolean.TRUE.equals(preparedByUrl.get(spell.getUrl()))));
        }

        return byLevel.entrySet().stream()
                .map(entry -> new SpellbookLevelGroupResponse(
                        entry.getKey(),
                        levelName(entry.getKey()),
                        entry.getValue().size(),
                        entry.getValue().stream().filter(SpellbookSpellResponse::isPrepared).count(),
                        entry.getValue()))
                .toList();
    }

    private static String levelName(long level) {
        return level == 0 ? "Заговоры" : String.format("%d уровень", level);
    }

    /**
     * Отметка изменения книги: правка состава заклинаний не меняет строку книги, поэтому
     * updated_at обновляется явно. Значение дублируется в загруженную сущность — bulk-update
     * не виден persistence context, а ответ должен отдавать свежий updatedAt.
     */
    private void touch(Spellbook spellbook) {
        Instant now = Instant.now();
        spellbookRepository.touch(spellbook.getId(), now);
        spellbook.setUpdatedAt(now);
    }

    private String nameOrDefault(SpellbookRequest request) {
        return request != null && StringUtils.hasText(request.getName())
                ? request.getName().trim()
                : DEFAULT_NAME;
    }

    private String currentUsername() {
        String username = SecurityUtils.getUser().getUsername();
        if (!StringUtils.hasText(username)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        return username;
    }

    private static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
