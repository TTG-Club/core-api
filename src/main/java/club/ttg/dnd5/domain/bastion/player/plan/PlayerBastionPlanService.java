package club.ttg.dnd5.domain.bastion.player.plan;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembership;
import club.ttg.dnd5.domain.bastion.player.client.GameRole;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionFacility;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Building;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Floor;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionAccess;
import club.ttg.dnd5.domain.bastion.repository.BastionFacilityRepository;
import club.ttg.dnd5.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * План бастиона: чтение для всех участников игры, правка — мастеру и игрокам с доступом.
 *
 * <p>Сооружение, убранное из выбора персонажа, с плана исчезает само: при чтении и
 * сохранении клетки несуществующих сооружений отбрасываются. Иначе план с такой клеткой
 * нельзя было бы сохранить, пока игрок вручную не нашёл бы её на холсте.</p>
 */
@Service
@RequiredArgsConstructor
public class PlayerBastionPlanService {
    private final PlayerBastionPlanRepository planRepository;
    private final BastionFacilityRepository facilityRepository;
    private final PlayerBastionAccess access;

    @Transactional(readOnly = true)
    public PlanResponse find(UUID bastionId) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        GameMembership membership = access.requireParticipant(bastion.getGameId(), userId);

        Optional<PlayerBastionPlan> plan = planRepository.findById(bastionId);
        PlanDocument document = plan.map(PlayerBastionPlan::getDocument).orElseGet(PlanDocument::empty);

        return new PlanResponse(bastionId,
                plan.map(PlayerBastionPlan::getVersion).orElse(0L),
                canEdit(bastion, membership, userId),
                withoutMissingFacilities(document, facilitySpaces(bastion)));
    }

    @Transactional
    public PlanResponse save(UUID bastionId, PlanRequest request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        GameMembership membership = access.requireParticipant(bastion.getGameId(), userId);
        if (!canEdit(bastion, membership, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "План рисуют мастер и игроки с доступом к бастиону");
        }

        PlayerBastionPlan plan = planRepository.findById(bastionId).orElseGet(() -> {
            PlayerBastionPlan created = new PlayerBastionPlan();
            created.setBastionId(bastionId);
            return created;
        });
        long currentVersion = Optional.ofNullable(plan.getVersion()).orElse(0L);
        if (currentVersion != request.version()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "План уже изменили, обновите страницу — ваши правки не сохранены");
        }

        Map<UUID, FacilitySpace> spaces = facilitySpaces(bastion);
        PlanDocument document = withoutMissingFacilities(request.document(), spaces);
        PlanRules.validate(document, spaces, facilityNames(bastion));

        plan.setDocument(document);
        PlayerBastionPlan saved = planRepository.saveAndFlush(plan);
        return new PlanResponse(bastionId, saved.getVersion(), true, saved.getDocument());
    }

    private static boolean canEdit(PlayerBastion bastion, GameMembership membership, UUID userId) {
        boolean member = bastion.getMembers().stream().anyMatch(item -> item.getUserId().equals(userId));
        return bastion.getStatus() != PlayerBastionStatus.ARCHIVED
                && (membership.role() == GameRole.MASTER || member);
    }

    private static Map<UUID, FacilitySpace> facilitySpaces(PlayerBastion bastion) {
        return facilities(bastion).stream()
                .collect(Collectors.toMap(PlayerBastionFacility::getId, PlayerBastionFacility::getSpace));
    }

    private Map<UUID, String> facilityNames(PlayerBastion bastion) {
        List<PlayerBastionFacility> facilities = facilities(bastion);
        Map<String, String> names = facilityRepository.findAllById(facilities.stream()
                        .map(PlayerBastionFacility::getFacilityUrl)
                        .collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(BastionFacility::getUrl, BastionFacility::getName));
        return facilities.stream().collect(Collectors.toMap(PlayerBastionFacility::getId,
                facility -> names.getOrDefault(facility.getFacilityUrl(), facility.getFacilityUrl())));
    }

    private static List<PlayerBastionFacility> facilities(PlayerBastion bastion) {
        return bastion.getMembers().stream()
                .flatMap(member -> member.getFacilities().stream())
                .toList();
    }

    /** Убирает клетки сооружений, которых больше нет в бастионе. */
    static PlanDocument withoutMissingFacilities(PlanDocument document, Map<UUID, ?> facilities) {
        if (document == null) {
            return PlanDocument.empty();
        }
        List<Building> buildings = Optional.ofNullable(document.buildings()).orElse(List.of()).stream()
                .map(building -> building == null ? null : new Building(building.id(), building.name(),
                        building.kind(), building.outline(),
                        Optional.ofNullable(building.floors()).orElse(List.of()).stream()
                                .map(floor -> floor == null ? null : new Floor(floor.level(),
                                        Optional.ofNullable(floor.cells()).orElse(List.of()).stream()
                                                .filter(cell -> cell != null && facilities.containsKey(cell.facilityId()))
                                                .toList(),
                                        floor.doors(), floor.windows(), floor.stairs()))
                                .toList()))
                .toList();
        return new PlanDocument(buildings,
                Optional.ofNullable(document.passages()).orElse(List.of()),
                Optional.ofNullable(document.walls()).orElse(List.of()));
    }
}
