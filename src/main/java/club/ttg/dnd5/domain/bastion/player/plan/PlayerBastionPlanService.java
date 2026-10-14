package club.ttg.dnd5.domain.bastion.player.plan;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionFacility;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.service.BastionViewer;
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
 * сохранении комнаты несуществующих сооружений становятся просто помещениями. Иначе план
 * с такой комнатой нельзя было бы сохранить, пока игрок вручную не нашёл бы её на холсте.</p>
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
        BastionViewer viewer = access.requireViewer(bastion, userId);
        return toResponse(bastion, canEdit(bastion, viewer));
    }

    /** План бастиона, открытого по ссылке «поделиться»: только чтение. */
    @Transactional(readOnly = true)
    public PlanResponse findByLink(PlayerBastion bastion) {
        return toResponse(bastion, false);
    }

    private PlanResponse toResponse(PlayerBastion bastion, boolean canEdit) {
        Optional<PlayerBastionPlan> plan = planRepository.findById(bastion.getId());
        PlanDocument document = plan.map(PlayerBastionPlan::getDocument).orElseGet(PlanDocument::empty);

        return new PlanResponse(bastion.getId(),
                plan.map(PlayerBastionPlan::getVersion).orElse(0L),
                canEdit,
                PlanRules.normalize(document, facilitySpaces(bastion)));
    }

    @Transactional
    public PlanResponse save(UUID bastionId, PlanRequest request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        if (!canEdit(bastion, viewer)) {
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
        PlanDocument document = PlanRules.normalize(request.document(), spaces);
        PlanRules.validate(document, spaces, facilityNames(bastion));

        plan.setDocument(document);
        PlayerBastionPlan saved = planRepository.saveAndFlush(plan);
        return new PlanResponse(bastionId, saved.getVersion(), true, saved.getDocument());
    }

    private static boolean canEdit(PlayerBastion bastion, BastionViewer viewer) {
        return bastion.getStatus() != PlayerBastionStatus.ARCHIVED && viewer.canEdit();
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
}
