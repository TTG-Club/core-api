package club.ttg.dnd5.domain.bastion.player.plan;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionFacility;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.service.BastionViewer;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionAccess;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionLimits;
import club.ttg.dnd5.domain.bastion.repository.BastionFacilityRepository;
import club.ttg.dnd5.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * План бастиона: чтение для всех участников игры, правка — мастеру и игрокам с доступом.
 *
 * <p>Сооружение, убранное из выбора персонажа, с плана исчезает само: при чтении и
 * сохранении комнаты несуществующих сооружений становятся просто помещениями. Иначе план
 * с такой комнатой нельзя было бы сохранить, пока игрок вручную не нашёл бы её на холсте.</p>
 *
 * <p>Предметы интерьера по подписке ставит только бастион владельца с действующей подпиской.
 * Уже стоящие такие предметы после окончания подписки остаются: их можно двигать и убирать,
 * но не добавлять новые.</p>
 *
 * <p>Защитную стену ставят только из достроенных блоков бастиона. Стена, нарисованная до
 * того, как блоки стали покупаться, остаётся: её можно убирать, но не наращивать, пока не
 * хватит блоков. При сохранении бастион запоминает, окружён ли он стеной полностью, — это
 * читает атака.</p>
 */
@Service
@RequiredArgsConstructor
public class PlayerBastionPlanService {
    private final PlayerBastionPlanRepository planRepository;
    private final BastionFacilityRepository facilityRepository;
    private final PlayerBastionAccess access;
    private final PlayerBastionLimits limits;

    @Transactional(readOnly = true)
    public PlanResponse find(UUID bastionId) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        boolean canEdit = canEdit(bastion, viewer);
        return toResponse(bastion, canEdit, canEdit && limits.isSubscriber(bastion.getOwnerId()));
    }

    /** План бастиона, открытого по ссылке «поделиться»: только чтение. */
    @Transactional(readOnly = true)
    public PlanResponse findByLink(PlayerBastion bastion) {
        return toResponse(bastion, false, false);
    }

    private PlanResponse toResponse(PlayerBastion bastion, boolean canEdit, boolean premium) {
        Optional<PlayerBastionPlan> plan = planRepository.findById(bastion.getId());
        PlanDocument document = plan.map(PlayerBastionPlan::getDocument).orElseGet(PlanDocument::empty);

        return new PlanResponse(bastion.getId(),
                plan.map(PlayerBastionPlan::getVersion).orElse(0L),
                canEdit,
                premium,
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

        boolean premium = limits.isSubscriber(bastion.getOwnerId());
        if (!premium && hasNewPremiumFurniture(plan.getDocument(), document)) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Предметы интерьера по подписке доступны, когда подписка есть у владельца бастиона");
        }

        if (exceedsWallBlocks(plan.getDocument(), document, bastion.getDefenseWallSquares())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "На плане больше клеток защитной стены, чем достроено блоков: %d"
                            .formatted(bastion.getDefenseWallSquares()));
        }
        boolean enclosed = PlanRules.isEnclosed(document);
        if (bastion.isDefenseWallsEnclosed() != enclosed) {
            bastion.setDefenseWallsEnclosed(enclosed);
        }

        plan.setDocument(document);
        PlayerBastionPlan saved = planRepository.saveAndFlush(plan);
        return new PlanResponse(bastionId, saved.getVersion(), true,
                premium, saved.getDocument());
    }

    /** В плане появился предмет по подписке, которого не было в сохранённой версии. */
    static boolean hasNewPremiumFurniture(PlanDocument saved, PlanDocument next) {
        Set<String> existing = saved == null || saved.furniture() == null ? Set.of() : saved.furniture().stream()
                .filter(item -> item.kind().premium())
                .map(PlanDocument.Furniture::id)
                .collect(Collectors.toSet());
        return next.furniture().stream()
                .anyMatch(item -> item.kind().premium() && !existing.contains(item.id()));
    }

    /**
     * Клеток стены больше, чем достроено блоков, и больше, чем было в сохранённой версии.
     * Ворота стоят на клетках стены и отдельно не считаются.
     */
    static boolean exceedsWallBlocks(PlanDocument saved, PlanDocument next, int blocks) {
        long before = saved == null || saved.defenseWalls() == null ? 0
                : saved.defenseWalls().stream().distinct().count();
        long after = next.defenseWalls().stream().distinct().count();
        return after > blocks && after > before;
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
