package club.ttg.dnd5.domain.user.service;

import club.ttg.dnd5.domain.common.service.slug.HomebrewSlug;
import club.ttg.dnd5.domain.user.model.UserHandle;
import club.ttg.dnd5.domain.user.repository.UserHandleRepository;
import club.ttg.dnd5.util.SlugifyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Резолвит замороженный slug-хендл пользователя для homebrew-url.
 * <p>
 * При первом обращении заводит хендл из логина ({@link SlugifyUtil} транслитерирует кириллицу),
 * разрешая коллизии разных логинов числовым суффиксом, и сохраняет навсегда. Повторные вызовы
 * возвращают тот же хендл (frozen).
 */
@Service
@RequiredArgsConstructor
public class UserHandleService {

    /** Запасная основа, если логин после слагификации пуст (например, был целиком из спецсимволов). */
    private static final String FALLBACK_HANDLE = "user";

    /** Предохранитель от бесконечного цикла при подборе уникального суффикса. */
    private static final int MAX_ATTEMPTS = 10_000;

    private final UserHandleRepository userHandleRepository;

    /**
     * Возвращает хендл пользователя, создавая и замораживая его при первом обращении.
     *
     * @param userId   uuid пользователя
     * @param username логин (основа хендла)
     * @return стабильный slug-хендл, уникальный среди всех пользователей
     */
    @Transactional
    public String resolveHandle(UUID userId, String username) {
        return userHandleRepository.findById(userId)
                .map(UserHandle::getHandle)
                .orElseGet(() -> createHandle(userId, username));
    }

    private String createHandle(UUID userId, String username) {
        String base = HomebrewSlug.slugify(SlugifyUtil.getSlug(username));
        if (base.isEmpty()) {
            base = FALLBACK_HANDLE;
        }

        String handle = base;
        for (int n = 2; userHandleRepository.existsByHandleIgnoreCase(handle); n++) {
            if (n > MAX_ATTEMPTS) {
                throw new IllegalStateException(
                        "Не удалось подобрать уникальный хендл для основы '" + base + "'");
            }
            handle = base + "-" + n;
        }

        return userHandleRepository.save(new UserHandle(userId, handle)).getHandle();
    }
}
