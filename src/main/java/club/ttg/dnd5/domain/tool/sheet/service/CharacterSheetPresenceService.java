package club.ttg.dnd5.domain.tool.sheet.service;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheetPresence;
import club.ttg.dnd5.domain.tool.sheet.repository.CharacterSheetPresenceRepository;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetPresenceResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetPresenceUserResponse;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.rest.dto.DisplayNameByUserIdResponse;
import club.ttg.dnd5.domain.user.service.DisplayNameService;
import club.ttg.dnd5.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Мягкая блокировка листа: кто держит его открытым на правку. Клиент шлёт отметку, пока лист
 * открыт, и в ответ узнаёт, у кого ещё он открыт, — чтобы предупредить «сейчас редактирует такой-то».
 * Запретов нет: одновременное сохранение и так отсекает версия листа (409), отметка лишь
 * предупреждает заранее.
 * <p>
 * Отмечаться может тот же круг, что открывает лист на правку: владелец и редакторы с выданным
 * правом. Зрителю по ссылке отметка не нужна — он ничего не меняет.
 */
@RequiredArgsConstructor
@Service
public class CharacterSheetPresenceService {

    /**
     * Сколько отметка считается живой. Клиент отмечается чаще (раз в 20 секунд), поэтому один
     * пропущенный запрос не гасит предупреждение у соседа.
     */
    static final Duration PRESENCE_TTL = Duration.ofSeconds(45);

    private static final String UNNAMED_USER = "Пользователь без имени";

    private final CharacterSheetService sheetService;
    private final CharacterSheetPresenceRepository presenceRepository;
    private final DisplayNameService displayNameService;

    /**
     * Отмечает, что лист открыт у текущего пользователя, и возвращает остальных, у кого он открыт.
     * Заодно чистит отметки этого листа, которые давно не освежались: ушедшие без прощания (закрытая
     * вкладка, пропавшая сеть) иначе копились бы в таблице.
     */
    @Transactional
    public CharacterSheetPresenceResponse heartbeat(UUID sheetId) {
        User user = SecurityUtils.getUser();
        CharacterSheet sheet = sheetService.getEditableActive(sheetId, user);
        Instant now = Instant.now();
        Instant freshSince = now.minus(PRESENCE_TTL);
        presenceRepository.deleteAllBySheetIdAndSeenAtBefore(sheet.getId(), freshSince);
        presenceRepository.upsert(UUID.randomUUID(), sheet.getId(), user.getUuid(), now);
        List<CharacterSheetPresence> others = presenceRepository
                .findAllBySheetIdAndUserIdNotAndSeenAtAfter(sheet.getId(), user.getUuid(), freshSince);
        return new CharacterSheetPresenceResponse(toUsers(others));
    }

    /**
     * Снимает отметку текущего пользователя — лист закрыт. Без проверки доступа: убрать можно только
     * свою отметку, а редактор, у которого право уже отозвали, тоже должен уметь уйти.
     */
    @Transactional
    public void leave(UUID sheetId) {
        User user = SecurityUtils.getUser();
        presenceRepository.deleteBySheetIdAndUserId(sheetId, user.getUuid());
    }

    /**
     * Имена и аватарки одним запросом; у пользователя без отображаемого имени — общая подпись,
     * логин наружу не отдаётся.
     */
    private List<CharacterSheetPresenceUserResponse> toUsers(List<CharacterSheetPresence> presences) {
        if (presences.isEmpty()) {
            return List.of();
        }
        Map<UUID, DisplayNameByUserIdResponse> names = displayNameService
                .resolveByUserIds(presences.stream().map(CharacterSheetPresence::getUserId).toList())
                .stream()
                .collect(Collectors.toMap(DisplayNameByUserIdResponse::userId, Function.identity()));
        return presences.stream()
                .map(presence -> names.get(presence.getUserId()))
                .map(name -> name != null
                        ? new CharacterSheetPresenceUserResponse(name.displayName(), name.avatarUrl())
                        : new CharacterSheetPresenceUserResponse(UNNAMED_USER, null))
                .toList();
    }
}
