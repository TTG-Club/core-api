package club.ttg.dnd5.domain.bastion.player.rest.controller;

import club.ttg.dnd5.domain.bastion.player.activity.ActivityRequests;
import club.ttg.dnd5.domain.bastion.player.activity.ActivityResponse;
import club.ttg.dnd5.domain.bastion.player.activity.PlayerBastionActivityService;
import club.ttg.dnd5.domain.bastion.player.plan.PlanRequest;
import club.ttg.dnd5.domain.bastion.player.plan.PlanResponse;
import club.ttg.dnd5.domain.bastion.player.plan.PlayerBastionPlanService;
import club.ttg.dnd5.domain.bastion.player.rest.dto.CreatePlayerBastionRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.FacilitySetupRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.JoinGameRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.MyPlayerBastionsResponse;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionGameResponse;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionResponse;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PrerequisiteConfirmationRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.UpdatePlayerBastionRequest;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionFacilityService;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionService;
import club.ttg.dnd5.domain.bastion.player.share.PlayerBastionEditorResponse;
import club.ttg.dnd5.domain.bastion.player.share.PlayerBastionShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Бастионы игроков: свои бастионы и бастионы в играх каталога. Только для авторизованных: {@code /api/v2/**} открыт
 * в конфигурации безопасности, поэтому доступ закрывает {@code @Secured("USER")}, а права
 * внутри игры проверяет сервис.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/player-bastions")
@Secured("USER")
@Tag(name = "Бастионы игроков", description = "Свои бастионы и бастионы в играх каталога")
public class PlayerBastionController {
    private final PlayerBastionService service;
    private final PlayerBastionFacilityService facilityService;
    private final PlayerBastionPlanService planService;
    private final PlayerBastionActivityService activityService;
    private final PlayerBastionShareService shareService;

    @Operation(summary = "Мои бастионы", description = "Свои бастионы и лимит: 3 без подписки, 12 с подпиской")
    @GetMapping("/my")
    public MyPlayerBastionsResponse findMine() {
        return service.findMine();
    }

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

    @Operation(summary = "Создать бастион", description = "Бастион принадлежит создавшему; лимит зависит от подписки")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerBastionResponse create(@Valid @RequestBody CreatePlayerBastionRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Изменить название и состав игроков",
            description = "Мастер игры, а вне игры — владелец")
    @PutMapping("/{id}")
    public PlayerBastionResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePlayerBastionRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Удалить бастион", description = "Только владелец; вместе с планом и журналами")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @Operation(summary = "Добавить бастион в игру", description = "Только владелец, участник этой игры")
    @PostMapping("/{id}/game")
    public PlayerBastionResponse joinGame(@PathVariable UUID id, @Valid @RequestBody JoinGameRequest request) {
        return service.joinGame(id, request);
    }

    @Operation(summary = "Убрать бастион из игры", description = "Владелец или мастер игры")
    @DeleteMapping("/{id}/game")
    public PlayerBastionResponse leaveGame(@PathVariable UUID id) {
        return service.leaveGame(id);
    }

    @Operation(summary = "Отправить бастион в архив", description = "Мастер игры, а вне игры — владелец")
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

    @Operation(summary = "Запустить бастион",
            description = "Мастер игры, а вне игры — владелец: закладка закончена, начинаются ходы")
    @PostMapping("/{id}/activate")
    public PlayerBastionResponse activate(@PathVariable UUID id) {
        return service.activate(id);
    }

    @Operation(summary = "Поделиться бастионом",
            description = "Владелец включает ссылку: по ней бастион открывается только на чтение")
    @PostMapping("/{id}/share")
    public PlayerBastionResponse share(@PathVariable UUID id) {
        return shareService.share(id);
    }

    @Operation(summary = "Отозвать ссылку", description = "Вместе со ссылкой снимаются все права на правку")
    @DeleteMapping("/{id}/share")
    public PlayerBastionResponse revokeShare(@PathVariable UUID id) {
        return shareService.revoke(id);
    }

    @Operation(summary = "Запросы на правку", description = "Ждущие ответа и разрешённые — для владельца")
    @GetMapping("/{id}/editors")
    public List<PlayerBastionEditorResponse> findEditors(@PathVariable UUID id) {
        return shareService.findEditors(id);
    }

    @Operation(summary = "Разрешить править бастион")
    @PostMapping("/{id}/editors/{editorId}/approve")
    public List<PlayerBastionEditorResponse> approveEditor(@PathVariable UUID id, @PathVariable UUID editorId) {
        return shareService.approve(id, editorId);
    }

    @Operation(summary = "Отклонить запрос или отозвать право на правку")
    @DeleteMapping("/{id}/editors/{editorId}")
    public List<PlayerBastionEditorResponse> removeEditor(@PathVariable UUID id, @PathVariable UUID editorId) {
        return shareService.remove(id, editorId);
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

    @Operation(summary = "Записать итог приказа", description = "Мастер или тот, кто отдал приказ, после его выполнения")
    @PutMapping("/{id}/orders/{orderId}/result")
    public PlayerBastionResponse writeOrderResult(@PathVariable UUID id,
                                                  @PathVariable UUID orderId,
                                                  @Valid @RequestBody ActivityRequests.OrderResult request) {
        return activityService.writeOrderResult(id, orderId, request);
    }

    @Operation(summary = "Сделать ход бастиона", description = "Только мастер игры: проходят 7 дней")
    @PostMapping("/{id}/turns")
    public PlayerBastionResponse performTurn(@PathVariable UUID id,
                                             @Valid @RequestBody ActivityRequests.Turn request) {
        return activityService.performTurn(id, request);
    }

    @Operation(summary = "Решение по событию бастиона",
            description = "Взятка, удачный случай, просьба о помощи, полученный предмет — игрок персонажа или мастер")
    @PostMapping("/{id}/events/{eventId}/decision")
    public PlayerBastionResponse decideEvent(@PathVariable UUID id,
                                             @PathVariable UUID eventId,
                                             @Valid @RequestBody ActivityRequests.EventDecision request) {
        return activityService.decideEvent(id, eventId, request);
    }

    @Operation(summary = "Отослать гостя бастиона", description = "Или найти беженцам новый дом")
    @DeleteMapping("/{id}/guests/{guestId}")
    public PlayerBastionResponse dismissGuest(@PathVariable UUID id, @PathVariable String guestId) {
        return activityService.dismissGuest(id, guestId);
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

    @Operation(summary = "Существо зверинца — защитник бастиона или нет",
            description = "Игрок этого персонажа или мастер")
    @PatchMapping("/{id}/facilities/{facilityId}/creatures/{creatureId}")
    public PlayerBastionResponse setCreatureDefender(@PathVariable UUID id,
                                                     @PathVariable UUID facilityId,
                                                     @PathVariable String creatureId,
                                                     @Valid @RequestBody ActivityRequests.CreatureDefender request) {
        return activityService.setCreatureDefender(id, facilityId, creatureId, request);
    }

    @Operation(summary = "Убрать существо из зверинца", description = "Например, оно погибло, защищая бастион")
    @DeleteMapping("/{id}/facilities/{facilityId}/creatures/{creatureId}")
    public PlayerBastionResponse removeCreature(@PathVariable UUID id,
                                                @PathVariable UUID facilityId,
                                                @PathVariable String creatureId) {
        return activityService.removeCreature(id, facilityId, creatureId);
    }

    @Operation(summary = "Убрать сооружение", description = "Только мастер игры — например, замена при повышении уровня")
    @DeleteMapping("/{id}/facilities/{facilityId}")
    public PlayerBastionResponse removeFacility(@PathVariable UUID id, @PathVariable UUID facilityId) {
        return activityService.removeFacility(id, facilityId);
    }
}
