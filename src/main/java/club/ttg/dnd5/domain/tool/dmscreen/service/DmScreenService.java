package club.ttg.dnd5.domain.tool.dmscreen.service;

import club.ttg.dnd5.domain.tool.dmscreen.model.DmScreen;
import club.ttg.dnd5.domain.tool.dmscreen.model.DmScreenWindow;
import club.ttg.dnd5.domain.tool.dmscreen.repository.DmScreenRepository;
import club.ttg.dnd5.domain.tool.dmscreen.rest.dto.DmScreenRequest;
import club.ttg.dnd5.domain.tool.dmscreen.rest.dto.DmScreenResponse;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Ширма Мастера пользователя: чтение и запись окон целиком. Ширма одна на пользователя и ищется по
 * uuid из JWT, поэтому чужую нельзя ни прочитать, ни перезаписать — идентификатор в запросе не
 * участвует вовсе.
 */
@RequiredArgsConstructor
@Service
public class DmScreenService {

    static final int MAX_WINDOWS = 100;
    static final int MAX_ID_LENGTH = 64;
    static final int MAX_PATH_LENGTH = 256;
    static final int MAX_TITLE_LENGTH = 256;
    static final int MAX_CONTENT_LENGTH = 20000;

    private static final String PAGE_PATH_PREFIX = "/";
    private static final String NOTE_PATH_PREFIX = "note:";

    private final DmScreenRepository screenRepository;

    /**
     * Ширма текущего пользователя. У того, кто её ещё не сохранял, — пустой список окон, а не 404:
     * для клиента это обычное начальное состояние.
     */
    public DmScreenResponse findMine() {
        return screenRepository.findByUserId(currentUserId())
                .map(screen -> new DmScreenResponse(screen.getWindows(), screen.getUpdatedAt()))
                .orElseGet(() -> new DmScreenResponse(List.of(), null));
    }

    /**
     * Заменяет окна ширмы целиком; первое сохранение создаёт запись. Транзакции на методе намеренно
     * нет: клиент пишет часто, и два первых сохранения могут прийти одновременно (обычное и
     * прощальное при закрытии вкладки). Проигравшее упрётся в уникальность владельца — тогда окна
     * записываются в уже созданную запись, а не теряются с ошибкой 500.
     */
    public void saveMine(DmScreenRequest request) {
        UUID userId = currentUserId();
        List<DmScreenWindow> windows = validate(request);
        try {
            store(userId, windows);
        } catch (DataIntegrityViolationException exception) {
            store(userId, windows);
        }
    }

    private void store(UUID userId, List<DmScreenWindow> windows) {
        DmScreen screen = screenRepository.findByUserId(userId).orElseGet(() -> {
            DmScreen created = new DmScreen();
            created.setUserId(userId);
            return created;
        });
        screen.setWindows(windows);
        screenRepository.saveAndFlush(screen);
    }

    /**
     * Пользователь из токена. Аноним и токен без uuid в subject (старый формат) — 401: ширму
     * не к кому привязать.
     */
    private static UUID currentUserId() {
        User user = SecurityUtils.getUserOrNull();
        if (user == null || user.getUuid() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        return user.getUuid();
    }

    private static List<DmScreenWindow> validate(DmScreenRequest request) {
        if (request == null || request.getWindows() == null) {
            throw badRequest("Не передан список окон ширмы");
        }
        List<DmScreenWindow> windows = request.getWindows();
        if (windows.size() > MAX_WINDOWS) {
            throw badRequest("На ширме может быть не больше " + MAX_WINDOWS + " окон");
        }
        for (int index = 0; index < windows.size(); index++) {
            validateWindow(windows.get(index), index + 1);
        }
        return new ArrayList<>(windows);
    }

    private static void validateWindow(DmScreenWindow window, int number) {
        if (window == null) {
            throw badRequest(number, "окно не заполнено");
        }
        String id = window.getId();
        if (id == null || id.isBlank()) {
            throw badRequest(number, "не указан идентификатор");
        }
        if (id.length() > MAX_ID_LENGTH) {
            throw badRequest(number, "идентификатор длиннее " + MAX_ID_LENGTH + " символов");
        }
        String path = window.getPath();
        if (path == null || path.isBlank()) {
            throw badRequest(number, "не указан адрес записи");
        }
        if (path.length() > MAX_PATH_LENGTH) {
            throw badRequest(number, "адрес записи длиннее " + MAX_PATH_LENGTH + " символов");
        }
        boolean note = path.startsWith(NOTE_PATH_PREFIX);
        if (!note && !path.startsWith(PAGE_PATH_PREFIX)) {
            throw badRequest(number, "адрес должен начинаться с «/» или с «note:»");
        }
        if (window.getTitle() == null) {
            throw badRequest(number, "не указан заголовок");
        }
        if (window.getTitle().length() > MAX_TITLE_LENGTH) {
            throw badRequest(number, "заголовок длиннее " + MAX_TITLE_LENGTH + " символов");
        }
        if (!isFinite(window.getX()) || !isFinite(window.getY())) {
            throw badRequest(number, "не указано положение");
        }
        if (!isFinite(window.getWidth()) || !isFinite(window.getHeight())) {
            throw badRequest(number, "не указан размер");
        }
        if (window.getWidth() <= 0 || window.getHeight() <= 0) {
            throw badRequest(number, "ширина и высота должны быть больше нуля");
        }
        if (window.getLayer() == null) {
            throw badRequest(number, "не указан слой");
        }
        if (window.getMinimized() == null) {
            throw badRequest(number, "не указано, свёрнуто ли окно");
        }
        String content = window.getContent();
        if (content != null && !note) {
            throw badRequest(number, "текст бывает только у заметок");
        }
        if (content != null && content.length() > MAX_CONTENT_LENGTH) {
            throw badRequest(number, "текст заметки длиннее " + MAX_CONTENT_LENGTH + " символов");
        }
    }

    private static boolean isFinite(Double value) {
        return value != null && Double.isFinite(value);
    }

    private static ApiException badRequest(int number, String reason) {
        return badRequest("Окно " + number + ": " + reason);
    }

    private static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }
}
