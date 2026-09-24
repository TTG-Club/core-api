package club.ttg.dnd5.domain.bastion.player.rest.controller;

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
}
