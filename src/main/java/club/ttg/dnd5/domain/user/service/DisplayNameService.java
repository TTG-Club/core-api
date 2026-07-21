package club.ttg.dnd5.domain.user.service;

import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.model.UserDisplayName;
import club.ttg.dnd5.domain.user.repository.UserDisplayNameRepository;
import club.ttg.dnd5.domain.user.rest.dto.DisplayNameByLoginResponse;
import club.ttg.dnd5.domain.user.rest.dto.DisplayNameResponse;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Отображаемое имя пользователя. core-api — владелец данных (сайтовый бэкенд);
 * auth-service и JWT не участвуют.
 *
 * Методы намеренно НЕ помечены {@code @Transactional}: каждый вызов репозитория
 * идёт своей транзакцией, поэтому нарушение уникального индекса не переводит
 * окружающую транзакцию в rollback-only и не ломает последующие чтения. Гонки
 * на уникальном имени ловятся через {@link DataIntegrityViolationException},
 * а {@code save()} по назначенному id работает как upsert (merge).
 */
@Service
@RequiredArgsConstructor
public class DisplayNameService {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}\\p{N}_\\s-]+$");
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 24;
    private static final int GENERATION_ATTEMPTS = 10;
    private static final int MAX_LOOKUP = 200;
    private static final int SEARCH_LIMIT = 20;

    /**
     * Подстроки, запрещённые в имени (регистронезависимо, без учёта пробелов) —
     * против выдачи себя за администрацию сайта.
     */
    private static final Set<String> RESERVED_SUBSTRINGS = Set.of(
            "admin", "админ", "administrator", "администратор",
            "moderator", "модератор", "модер",
            "support", "поддержк", "owner", "овнер",
            "root", "superuser", "суперюзер", "ttgclub", "ттгклаб");

    private final UserDisplayNameRepository repository;
    private final DisplayNameGenerator generator;

    /**
     * Возвращает отображаемое имя текущего пользователя, лениво создавая запись
     * со случайным дефолтом при первом обращении.
     */
    public DisplayNameResponse getOrCreateForCurrentUser() {
        User user = SecurityUtils.getUser();
        UUID userId = user.getUuid();

        return new DisplayNameResponse(
                repository.findById(userId)
                        .map(UserDisplayName::getDisplayName)
                        .orElseGet(() -> createDefault(userId, user.getUsername())));
    }

    /**
     * Меняет отображаемое имя текущего пользователя после проверок формата,
     * зарезервированных слов и уникальности.
     */
    public DisplayNameResponse updateForCurrentUser(String requested) {
        User user = SecurityUtils.getUser();
        UUID userId = user.getUuid();
        String username = user.getUsername();
        String name = normalize(requested);

        validate(name, userId);

        UserDisplayName entity = repository.findById(userId).orElseGet(() -> {
            UserDisplayName created = new UserDisplayName();
            created.setUserId(userId);
            created.setCreatedAt(Instant.now());
            return created;
        });
        entity.setUsername(username);
        entity.setDisplayName(name);
        entity.setUpdatedAt(Instant.now());

        try {
            repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException ex) {
            // Гонка по уникальному индексу: имя заняли между проверкой и сохранением.
            throw new ApiException(HttpStatus.CONFLICT, "Это имя уже занято");
        }

        return new DisplayNameResponse(name);
    }

    /**
     * Резолвит логины в отображаемые имена — для публичных рейтингов (таблица охотников
     * за багами). Возвращает пары только для логинов, у которых имя задано; неизвестные
     * логины опускаются (не подтверждают существование), поэтому вызывающий откатывается
     * к логину сам. Размер входа ограничен {@link #MAX_LOOKUP}.
     */
    public List<DisplayNameByLoginResponse> resolveByLogins(Collection<String> logins) {
        if (logins == null || logins.isEmpty()) {
            return List.of();
        }
        Set<String> loweredLogins = logins.stream()
                .filter(login -> login != null && !login.isBlank())
                .limit(MAX_LOOKUP)
                .map(login -> login.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        if (loweredLogins.isEmpty()) {
            return List.of();
        }
        return repository.findAllByUsernameLowerIn(loweredLogins).stream()
                .map(entity -> new DisplayNameByLoginResponse(entity.getUsername(), entity.getDisplayName()))
                .toList();
    }

    /**
     * Поиск пользователей по отображаемому имени (подстрока) — для подсказок в админке.
     * Пустой или слишком короткий запрос (&lt; 2 символов) даёт пустой результат.
     * Размер выдачи ограничен {@link #SEARCH_LIMIT}.
     */
    public List<DisplayNameByLoginResponse> searchByDisplayName(String query) {
        String normalized = query == null ? "" : query.trim();
        if (normalized.length() < MIN_LENGTH) {
            return List.of();
        }
        return repository.searchByDisplayName(normalized, PageRequest.of(0, SEARCH_LIMIT)).stream()
                .map(entity -> new DisplayNameByLoginResponse(entity.getUsername(), entity.getDisplayName()))
                .toList();
    }

    /**
     * Создаёт запись со случайным уникальным именем. Гонку по первичному ключу
     * (два первых запроса одного пользователя) гасит повторным чтением.
     */
    private String createDefault(UUID userId, String username) {
        String name = generateUniqueDefault();

        UserDisplayName entity = new UserDisplayName();
        entity.setUserId(userId);
        entity.setUsername(username);
        entity.setDisplayName(name);
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        try {
            repository.saveAndFlush(entity);
            return name;
        } catch (DataIntegrityViolationException ex) {
            return repository.findById(userId)
                    .map(UserDisplayName::getDisplayName)
                    .orElseGet(() -> createDefault(userId, username));
        }
    }

    /**
     * Подбирает свободное случайное имя: базовое «Прилагательное Существительное»,
     * при коллизии — с числовым суффиксом.
     */
    private String generateUniqueDefault() {
        String base = generator.nextBaseName();
        if (base.length() <= MAX_LENGTH && !repository.existsByDisplayNameIgnoreCase(base)) {
            return base;
        }
        // При коллизии — короткая основа «Существительное Число», гарантированно ≤ 24.
        for (int attempt = 0; attempt < GENERATION_ATTEMPTS; attempt++) {
            String candidate = generator.nextNoun() + " " + generator.nextSuffix();
            if (candidate.length() <= MAX_LENGTH && !repository.existsByDisplayNameIgnoreCase(candidate)) {
                return candidate;
            }
        }
        return generator.nextNoun() + generator.nextSuffix();
    }

    private void validate(String name, UUID userId) {
        if (name.length() < MIN_LENGTH || name.length() > MAX_LENGTH) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Имя должно быть от 2 до 24 символов");
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Только буквы, цифры, пробелы, дефисы и подчёркивания");
        }
        if (isReserved(name)) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Это имя зарезервировано");
        }
        if (repository.existsByDisplayNameIgnoreCaseAndUserIdNot(name, userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Это имя уже занято");
        }
    }

    /**
     * Приводит имя к каноничному виду: без крайних пробелов, внутренние пробелы схлопнуты.
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private boolean isReserved(String name) {
        String collapsed = name.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        return RESERVED_SUBSTRINGS.stream().anyMatch(collapsed::contains);
    }
}
