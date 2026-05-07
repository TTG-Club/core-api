package club.ttg.dnd5.domain.initiative.rest.controller;

import club.ttg.dnd5.domain.initiative.model.EncounterDifficulty;
import club.ttg.dnd5.domain.initiative.rest.dto.ActiveParticipantResponse;
import club.ttg.dnd5.domain.initiative.rest.dto.HpAmountRequest;
import club.ttg.dnd5.domain.initiative.rest.dto.HpUpdateRequest;
import club.ttg.dnd5.domain.initiative.rest.dto.InitiativeParticipantRequest;
import club.ttg.dnd5.domain.initiative.rest.dto.InitiativeTrackerCreateRequest;
import club.ttg.dnd5.domain.initiative.rest.dto.InitiativeTrackerResponse;
import club.ttg.dnd5.domain.initiative.rest.dto.InitiativeTrackerUpdateRequest;
import club.ttg.dnd5.domain.initiative.rest.dto.ParticipantStateRequest;
import club.ttg.dnd5.domain.initiative.rest.dto.RollInitiativeRequest;
import club.ttg.dnd5.domain.initiative.service.InitiativeTrackerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Трекер инициативы", description = "REST API для управления инициативой, раундами, участниками боя и хитами")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/initiative-trackers")
public class InitiativeTrackerController {
    private final InitiativeTrackerService service;

    @Operation(summary = "Получить текущий активный трекер инициативы пользователя")
    @GetMapping("/current")
    public InitiativeTrackerResponse current() {
        return service.current();
    }

    @Operation(summary = "Создать трекер инициативы")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InitiativeTrackerResponse create(@RequestBody InitiativeTrackerCreateRequest request) {
        return service.create(request);
    }

    @GetMapping("/{trackerId}")
    public InitiativeTrackerResponse find(@PathVariable UUID trackerId) {
        return service.find(trackerId);
    }

    @PatchMapping("/{trackerId}")
    public InitiativeTrackerResponse update(@PathVariable UUID trackerId, @RequestBody InitiativeTrackerUpdateRequest request) {
        return service.update(trackerId, request);
    }

    @PatchMapping("/{trackerId}/settings")
    public InitiativeTrackerResponse settings(@PathVariable UUID trackerId, @RequestBody InitiativeTrackerUpdateRequest request) {
        return service.update(trackerId, request);
    }

    @DeleteMapping("/{trackerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID trackerId) {
        service.delete(trackerId);
    }

    @PostMapping("/{trackerId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    public InitiativeTrackerResponse addParticipants(@PathVariable UUID trackerId, @RequestBody InitiativeParticipantRequest request) {
        return service.addParticipants(trackerId, request);
    }

    @PatchMapping("/{trackerId}/participants/{participantId}")
    public InitiativeTrackerResponse updateParticipant(
            @PathVariable UUID trackerId,
            @PathVariable UUID participantId,
            @RequestBody InitiativeParticipantRequest request) {
        return service.updateParticipant(trackerId, participantId, request);
    }

    @DeleteMapping("/{trackerId}/participants/{participantId}")
    public InitiativeTrackerResponse removeParticipant(@PathVariable UUID trackerId, @PathVariable UUID participantId) {
        return service.removeParticipant(trackerId, participantId);
    }

    @PostMapping("/{trackerId}/participants/{participantId}/roll-initiative")
    public InitiativeTrackerResponse rollInitiative(
            @PathVariable UUID trackerId,
            @PathVariable UUID participantId,
            @RequestBody RollInitiativeRequest request) {
        return service.rollInitiative(trackerId, participantId, request);
    }

    @PostMapping("/{trackerId}/participants/bulk-roll-initiative")
    public InitiativeTrackerResponse bulkRollInitiative(@PathVariable UUID trackerId) {
        return service.bulkRollInitiative(trackerId);
    }

    @PostMapping("/{trackerId}/start")
    public InitiativeTrackerResponse start(@PathVariable UUID trackerId) {
        return service.start(trackerId);
    }

    @PostMapping("/{trackerId}/finish")
    public InitiativeTrackerResponse finish(@PathVariable UUID trackerId) {
        return service.finish(trackerId);
    }

    @PostMapping("/{trackerId}/next-turn")
    public InitiativeTrackerResponse nextTurn(@PathVariable UUID trackerId) {
        return service.nextTurn(trackerId);
    }

    @PostMapping("/{trackerId}/previous-turn")
    public InitiativeTrackerResponse previousTurn(@PathVariable UUID trackerId) {
        return service.previousTurn(trackerId);
    }

    @PostMapping("/{trackerId}/next-round")
    public InitiativeTrackerResponse nextRound(@PathVariable UUID trackerId) {
        return service.nextRound(trackerId);
    }

    @PostMapping("/{trackerId}/previous-round")
    public InitiativeTrackerResponse previousRound(@PathVariable UUID trackerId) {
        return service.previousRound(trackerId);
    }

    @PostMapping("/{trackerId}/reroll-round")
    public InitiativeTrackerResponse rerollRound(@PathVariable UUID trackerId) {
        return service.rerollRound(trackerId);
    }

    @PostMapping("/{trackerId}/participants/{participantId}/damage")
    public InitiativeTrackerResponse damage(
            @PathVariable UUID trackerId,
            @PathVariable UUID participantId,
            @RequestBody HpAmountRequest request) {
        return service.damage(trackerId, participantId, request);
    }

    @PostMapping("/{trackerId}/participants/{participantId}/heal")
    public InitiativeTrackerResponse heal(
            @PathVariable UUID trackerId,
            @PathVariable UUID participantId,
            @RequestBody HpAmountRequest request) {
        return service.heal(trackerId, participantId, request);
    }

    @PostMapping("/{trackerId}/participants/{participantId}/temporary-hp")
    public InitiativeTrackerResponse temporaryHp(
            @PathVariable UUID trackerId,
            @PathVariable UUID participantId,
            @RequestBody HpAmountRequest request) {
        return service.temporaryHp(trackerId, participantId, request);
    }

    @PatchMapping("/{trackerId}/participants/{participantId}/hp")
    public InitiativeTrackerResponse updateHp(
            @PathVariable UUID trackerId,
            @PathVariable UUID participantId,
            @RequestBody HpUpdateRequest request) {
        return service.updateHp(trackerId, participantId, request);
    }

    @PatchMapping("/{trackerId}/participants/{participantId}/state")
    public InitiativeTrackerResponse updateState(
            @PathVariable UUID trackerId,
            @PathVariable UUID participantId,
            @RequestBody ParticipantStateRequest request) {
        return service.updateState(trackerId, participantId, request);
    }

    @GetMapping("/{trackerId}/difficulty")
    public EncounterDifficulty difficulty(@PathVariable UUID trackerId) {
        return service.difficulty(trackerId);
    }

    @PostMapping("/{trackerId}/recalculate-difficulty")
    public InitiativeTrackerResponse recalculateDifficulty(@PathVariable UUID trackerId) {
        return service.recalculateDifficulty(trackerId);
    }

    @GetMapping("/{trackerId}/active")
    public ActiveParticipantResponse active(@PathVariable UUID trackerId) {
        return service.active(trackerId);
    }

    @PostMapping("/{trackerId}/share")
    public InitiativeTrackerResponse share(@PathVariable UUID trackerId) {
        return service.share(trackerId);
    }

    @DeleteMapping("/{trackerId}/share")
    public InitiativeTrackerResponse unshare(@PathVariable UUID trackerId) {
        return service.unshare(trackerId);
    }

    @GetMapping("/shared/{shareToken}")
    public InitiativeTrackerResponse shared(@PathVariable String shareToken) {
        return service.shared(shareToken);
    }

    @GetMapping("/shared/{shareToken}/active")
    public ActiveParticipantResponse sharedActive(@PathVariable String shareToken) {
        return service.sharedActive(shareToken);
    }
}
