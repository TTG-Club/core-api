package club.ttg.dnd5.domain.tool.tracker.rest.controller;

import club.ttg.dnd5.domain.tool.tracker.rest.dto.ParticipantAddRequest;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.ParticipantUpdateRequest;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.TrackerDetailedResponse;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.TrackerRequest;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.TrackerShortResponse;
import club.ttg.dnd5.domain.tool.tracker.service.InitiativeTrackerService;
import club.ttg.dnd5.domain.tool.tracker.service.TrackerCreationRateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static club.ttg.dnd5.domain.tool.tracker.service.InitiativeTrackerService.TRACKER_KEY_HEADER;

/**
 * Доступ: трекер с владельцем — только владельцу (JWT); анонимный трекер — по секретному
 * ключу из заголовка {@code X-Tracker-Key}, который возвращается один раз при создании.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/tools/initiative")
@Tag(name = "Трекер инициативы",
        description = "REST API трекера инициативы: сборка энкаунтера, броски инициативы и порядок ходов")
public class InitiativeTrackerController {

    private final InitiativeTrackerService trackerService;

    @Operation(summary = "Создание трекера: авторизованному — до 10; анониму ключ доступа "
            + "возвращается один раз в поле accessKey (сохранить на клиенте)")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public TrackerDetailedResponse create(@RequestBody(required = false) @Valid final TrackerRequest request,
                                          final HttpServletRequest httpRequest) {
        return trackerService.create(request, TrackerCreationRateLimiter.resolveClientIp(httpRequest));
    }

    @Operation(summary = "Трекеры текущего пользователя с датами создания (история); "
            + "includeDeleted=true — вместе с удалёнными")
    @GetMapping
    public List<TrackerShortResponse> findMine(
            @RequestParam(required = false, defaultValue = "false")
            @Schema(description = "Включить удалённые трекеры (полная история)") final boolean includeDeleted) {
        return trackerService.findMine(includeDeleted);
    }

    @Operation(summary = "Трекер с участниками в порядке хода")
    @GetMapping("/{id}")
    public TrackerDetailedResponse findById(
            @PathVariable final UUID id,
            @RequestHeader(value = TRACKER_KEY_HEADER, required = false) final String trackerKey) {
        return trackerService.findById(id, trackerKey);
    }

    @Operation(summary = "Обновление трекера: имя и/или опция «новая инициатива каждый раунд» "
            + "(rerollEachRound). Применяются только переданные поля")
    @PutMapping("/{id}")
    public TrackerDetailedResponse updateSettings(
            @PathVariable final UUID id,
            @RequestBody @Valid final TrackerRequest request,
            @RequestHeader(value = TRACKER_KEY_HEADER, required = false) final String trackerKey) {
        return trackerService.updateSettings(id, request, trackerKey);
    }

    @Operation(summary = "Удаление трекера: у владельца — мягкое (остаётся в истории, хранятся "
            + "последние 30 удалённых, участники вычищаются), анонимный удаляется полностью")
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable final UUID id,
            @RequestHeader(value = TRACKER_KEY_HEADER, required = false) final String trackerKey) {
        trackerService.delete(id, trackerKey);
    }

    @Operation(summary = "Добавление участников: игрок (name + initiativeBonus) или существа из "
            + "бестиария (creatureUrl + count). В идущем бою новичок сразу получает бросок инициативы. "
            + "Лимиты: 50 игроков и 100 существ на трекер")
    @PostMapping("/{id}/participants")
    public TrackerDetailedResponse addParticipants(
            @PathVariable final UUID id,
            @RequestBody @Valid final ParticipantAddRequest request,
            @RequestHeader(value = TRACKER_KEY_HEADER, required = false) final String trackerKey) {
        return trackerService.addParticipants(id, request, trackerKey);
    }

    @Operation(summary = "Правка участника: имя, бонус инициативы, ручной результат d20, признак "
            + "«повержен» (dead). Применяются только заполненные поля. Работает и во время боя — "
            + "порядок хода пересобирается")
    @PutMapping("/{id}/participants/{participantId}")
    public TrackerDetailedResponse updateParticipant(
            @PathVariable final UUID id,
            @PathVariable final UUID participantId,
            @RequestBody @Valid final ParticipantUpdateRequest request,
            @RequestHeader(value = TRACKER_KEY_HEADER, required = false) final String trackerKey) {
        return trackerService.updateParticipant(id, participantId, request, trackerKey);
    }

    @Operation(summary = "Прокинуть инициативу одному участнику (d20 + бонус). Бой не начинает, "
            + "остальных не трогает — для броска по одному")
    @PostMapping("/{id}/participants/{participantId}/roll")
    public TrackerDetailedResponse rollParticipant(
            @PathVariable final UUID id,
            @PathVariable final UUID participantId,
            @RequestHeader(value = TRACKER_KEY_HEADER, required = false) final String trackerKey) {
        return trackerService.rollParticipant(id, participantId, trackerKey);
    }

    @Operation(summary = "Удаление участника из трекера; если сейчас его ход — ход переходит следующему живому")
    @DeleteMapping("/{id}/participants/{participantId}")
    public TrackerDetailedResponse deleteParticipant(
            @PathVariable final UUID id,
            @PathVariable final UUID participantId,
            @RequestHeader(value = TRACKER_KEY_HEADER, required = false) final String trackerKey) {
        return trackerService.deleteParticipant(id, participantId, trackerKey);
    }

    @Operation(summary = "Прокинуть инициативу всем: d20 + бонус каждому участнику, сортировка по "
            + "правилам D&D (бонус инициативы, затем «монетка»). Бой НЕ начинает — старт отдельным "
            + "действием (/start). В подготовке статус остаётся PREPARING (ход не назначается); "
            + "в идущем бою это ре-ролл — раунд с начала, ход первому живому")
    @PostMapping("/{id}/roll")
    public TrackerDetailedResponse rollInitiative(
            @PathVariable final UUID id,
            @RequestHeader(value = TRACKER_KEY_HEADER, required = false) final String trackerKey) {
        return trackerService.rollInitiative(id, trackerKey);
    }

    @Operation(summary = "Начать бой, сохранив уже введённые броски: у кого инициатива уже задана "
            + "(ручной ввод или бросок) — итог переходит в бой как есть; остальным инициатива 0 "
            + "(без доброса d20 и без бонуса). Порядок по итогу, не брошенные (0) — в конце. Ход "
            + "первому живому (в отличие от /roll — случайный d20 + бонус всем и полная сортировка)")
    @PostMapping("/{id}/start")
    public TrackerDetailedResponse start(
            @PathVariable final UUID id,
            @RequestHeader(value = TRACKER_KEY_HEADER, required = false) final String trackerKey) {
        return trackerService.start(id, trackerKey);
    }

    @Operation(summary = "Следующий ход; после последнего участника — новый раунд и ход первому")
    @PostMapping("/{id}/turn/next")
    public TrackerDetailedResponse nextTurn(
            @PathVariable final UUID id,
            @RequestHeader(value = TRACKER_KEY_HEADER, required = false) final String trackerKey) {
        return trackerService.nextTurn(id, trackerKey);
    }

    @Operation(summary = "Откат хода на шаг назад; с первого участника раунда — к последнему живому "
            + "предыдущего раунда (round - 1). На первом ходу первого раунда не откатывает — возвращает "
            + "текущее состояние. Броски инициативы не восстанавливаются (актуально при rerollEachRound)")
    @PostMapping("/{id}/turn/prev")
    public TrackerDetailedResponse prevTurn(
            @PathVariable final UUID id,
            @RequestHeader(value = TRACKER_KEY_HEADER, required = false) final String trackerKey) {
        return trackerService.prevTurn(id, trackerKey);
    }

    @Operation(summary = "Завершить бой: броски очищаются, состав участников сохраняется, "
            + "трекер возвращается в подготовку")
    @PostMapping("/{id}/reset")
    public TrackerDetailedResponse reset(
            @PathVariable final UUID id,
            @RequestHeader(value = TRACKER_KEY_HEADER, required = false) final String trackerKey) {
        return trackerService.reset(id, trackerKey);
    }
}
