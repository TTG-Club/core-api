package club.ttg.dnd5.domain.bastion.player.rest.controller;

import club.ttg.dnd5.domain.bastion.player.activity.ActivityRequests;
import club.ttg.dnd5.domain.bastion.player.activity.ActivityResponse;
import club.ttg.dnd5.domain.bastion.player.activity.PlayerBastionActivityService;
import club.ttg.dnd5.domain.bastion.player.plan.PlanRequest;
import club.ttg.dnd5.domain.bastion.player.plan.PlanResponse;
import club.ttg.dnd5.domain.bastion.player.plan.PlayerBastionPlanService;
import club.ttg.dnd5.domain.bastion.player.rest.dto.CreatePlayerBastionRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.FacilitySetupRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionGameResponse;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionResponse;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PrerequisiteConfirmationRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.UpdatePlayerBastionRequest;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionFacilityService;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Бастионы игроков в играх каталога. Только для авторизованных: {@code /api/v2/**} открыт
 * в конфигурации безопасности, поэтому доступ закрывает {@code @Secured("USER")}, а права
 * внутри игры проверяет сервис.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/player-bastions")
@Secured("USER")
@Tag(name = "Бастионы игроков", description = "Бастионы в играх каталога")
public class PlayerBastionController {
    private final PlayerBastionService service;
    private final PlayerBastionFacilityService facilityService;
    private final PlayerBastionPlanService planService;
    private final PlayerBastionActivityService activityService;

    @Operation(summary = "Бастионы игры", description = "Все бастионы игры и, для мастера, игроки, которым можно дать доступ")
    @GetMapping("/games/{gameId}")
    public PlayerBastionGameResponse findForGame(@PathVariable UUID gameId) {
        return service.findForGame(gameId);
    }

    @Operation(summary = "Бастион")
    @GetMapping("/{id}")
    public PlayerBastionResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @Operation(summary = "Создать бастион", description = "Только мастер игры")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerBastionResponse create(@Valid @RequestBody CreatePlayerBastionRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Изменить название и состав игроков", description = "Только мастер игры")
    @PutMapping("/{id}")
    public PlayerBastionResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePlayerBastionRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Отправить бастион в архив", description = "Только мастер игры")
    @PostMapping("/{id}/archive")
    public PlayerBastionResponse archive(@PathVariable UUID id) {
        return service.archive(id);
    }

    @Operation(summary = "Стартовый выбор сооружений персонажа",
            description = "Пока бастион в закладке: игрок — своего персонажа, мастер — любого")
    @PutMapping("/{id}/members/{memberId}/facilities")
    public PlayerBastionResponse updateSetup(@PathVariable UUID id,
                                             @PathVariable UUID memberId,
                                             @Valid @RequestBody FacilitySetupRequest request) {
        return facilityService.updateSetup(id, memberId, request);
    }

    @Operation(summary = "Подтвердить требование сооружения", description = "Только мастер игры")
    @PutMapping("/{id}/facilities/{facilityId}/prerequisite")
    public PlayerBastionResponse confirmPrerequisite(@PathVariable UUID id,
                                                     @PathVariable UUID facilityId,
                                                     @Valid @RequestBody PrerequisiteConfirmationRequest request) {
        return facilityService.confirmPrerequisite(id, facilityId, request.confirmed());
    }

    @Operation(summary = "Запустить бастион", description = "Только мастер игры: закладка закончена, начинаются ходы")
    @PostMapping("/{id}/activate")
    public PlayerBastionResponse activate(@PathVariable UUID id) {
        return service.activate(id);
    }

    @Operation(summary = "План бастиона", description = "Смотреть может любой участник игры")
    @GetMapping("/{id}/plan")
    public PlanResponse findPlan(@PathVariable UUID id) {
        return planService.find(id);
    }

    @Operation(summary = "Сохранить план бастиона",
            description = "Мастер и игроки с доступом; версия устарела — 409")
    @PutMapping("/{id}/plan")
    public PlanResponse savePlan(@PathVariable UUID id, @Valid @RequestBody PlanRequest request) {
        return planService.save(id, request);
    }

    @Operation(summary = "Журналы бастиона", description = "Приказы, ходы и казна — свежие первыми")
    @GetMapping("/{id}/activity")
    public ActivityResponse findActivity(@PathVariable UUID id) {
        return activityService.findActivity(id);
    }

    @Operation(summary = "Отдать приказ",
            description = "Сооружению — игрок этого персонажа или мастер; «Обслуживать» — всему бастиону")
    @PostMapping("/{id}/orders")
    public PlayerBastionResponse giveOrder(@PathVariable UUID id,
                                           @Valid @RequestBody ActivityRequests.GiveOrder request) {
        return activityService.giveOrder(id, request);
    }

    @Operation(summary = "Отменить приказ", description = "Только в тот же ход; цена возвращается в казну")
    @DeleteMapping("/{id}/orders/{orderId}")
    public PlayerBastionResponse cancelOrder(@PathVariable UUID id, @PathVariable UUID orderId) {
        return activityService.cancelOrder(id, orderId);
    }

    @Operation(summary = "Сделать ход бастиона", description = "Только мастер игры: проходят 7 дней")
    @PostMapping("/{id}/turns")
    public PlayerBastionResponse performTurn(@PathVariable UUID id,
                                             @Valid @RequestBody ActivityRequests.Turn request) {
        return activityService.performTurn(id, request);
    }

    @Operation(summary = "Пополнить или списать казну", description = "Только мастер игры")
    @PostMapping("/{id}/treasury")
    public PlayerBastionResponse adjustTreasury(@PathVariable UUID id,
                                                @Valid @RequestBody ActivityRequests.Treasury request) {
        return activityService.adjustTreasury(id, request);
    }

    @Operation(summary = "Построить базовое сооружение", description = "Цена и срок — из правил бастиона")
    @PostMapping("/{id}/members/{memberId}/facilities/basic")
    public PlayerBastionResponse buildBasic(@PathVariable UUID id,
                                            @PathVariable UUID memberId,
                                            @Valid @RequestBody ActivityRequests.BuildBasic request) {
        return activityService.buildBasic(id, memberId, request);
    }

    @Operation(summary = "Новое специализированное сооружение", description = "Персонаж дорос до следующего лимита")
    @PostMapping("/{id}/members/{memberId}/facilities/special")
    public PlayerBastionResponse addSpecial(@PathVariable UUID id,
                                            @PathVariable UUID memberId,
                                            @Valid @RequestBody ActivityRequests.AddSpecial request) {
        return activityService.addSpecial(id, memberId, request);
    }

    @Operation(summary = "Расширить сооружение")
    @PostMapping("/{id}/facilities/{facilityId}/enlarge")
    public PlayerBastionResponse enlarge(@PathVariable UUID id, @PathVariable UUID facilityId) {
        return activityService.enlarge(id, facilityId);
    }

    @Operation(summary = "Убрать сооружение", description = "Только мастер игры — например, замена при повышении уровня")
    @DeleteMapping("/{id}/facilities/{facilityId}")
    public PlayerBastionResponse removeFacility(@PathVariable UUID id, @PathVariable UUID facilityId) {
        return activityService.removeFacility(id, facilityId);
    }
}
