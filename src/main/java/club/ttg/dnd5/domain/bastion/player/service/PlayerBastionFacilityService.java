package club.ttg.dnd5.domain.bastion.player.service;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.BastionRules;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembership;
import club.ttg.dnd5.domain.bastion.player.client.GameRole;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionFacility;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionMember;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.model.SelectedChoice;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionRepository;
import club.ttg.dnd5.domain.bastion.player.rest.dto.FacilitySetupRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionResponse;
import club.ttg.dnd5.domain.bastion.repository.BastionFacilityRepository;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Сооружения персонажей в бастионе: стартовый выбор и подтверждение требований.
 *
 * <p>Стартовый выбор открыт, пока бастион в закладке: игрок меняет сооружения своего
 * персонажа, мастер — любого. Правила проверяет {@link PlayerBastionSetupRules}.
 * Требование сооружения (фокусировка и т.п.) лист персонажа пока не проверяет — его
 * подтверждает мастер.</p>
 */
@Service
@RequiredArgsConstructor
public class PlayerBastionFacilityService {
    private final PlayerBastionRepository bastionRepository;
    private final BastionFacilityRepository facilityRepository;
    private final PlayerBastionAccess access;
    private final PlayerBastionResponseMapper mapper;
    private final EntityManager entityManager;

    /** Заменяет стартовый выбор сооружений персонажа. */
    @Transactional
    public PlayerBastionResponse updateSetup(UUID bastionId, UUID memberId, FacilitySetupRequest request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        GameMembership membership = access.requireParticipant(bastion.getGameId(), userId);
        PlayerBastionMember member = findMember(bastion, memberId);
        if (membership.role() != GameRole.MASTER && !member.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Сооружения персонажа выбирает его игрок или мастер");
        }
        if (bastion.getStatus() != PlayerBastionStatus.SETUP) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Стартовые сооружения выбираются, пока бастион в закладке");
        }
        PlayerBastionAccess.requireVersion(bastion, request.version());

        Map<String, BastionFacility> reference = loadReference(Stream.concat(
                request.basic().stream().map(FacilitySetupRequest.Basic::facilityUrl),
                request.special().stream().map(FacilitySetupRequest.Special::facilityUrl)));
        PlayerBastionSetupRules.validate(member.getCharacterLevel(), request, reference);

        replaceFacilities(member, request, reference);
        return mapper.toResponse(saveWithNewVersion(bastion), membership, userId);
    }

    /** Мастер подтверждает или снимает подтверждение требования сооружения. */
    @Transactional
    public PlayerBastionResponse confirmPrerequisite(UUID bastionId, UUID facilityId, boolean confirmed) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        GameMembership membership = access.requireMaster(bastion.getGameId(), userId);
        PlayerBastionAccess.requireNotArchived(bastion);

        PlayerBastionFacility facility = bastion.getMembers().stream()
                .flatMap(member -> member.getFacilities().stream())
                .filter(item -> item.getId().equals(facilityId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Сооружение %s не найдено".formatted(facilityId)));
        facility.setPrerequisiteConfirmed(confirmed);

        return mapper.toResponse(saveWithNewVersion(bastion), membership, userId);
    }

    /**
     * Сохраняет бастион с новой версией. Сооружения лежат в коллекции участника, и
     * их правка сама версию бастиона не поднимает — а без этого параллельная правка
     * состава мастером или другим игроком не получила бы 409 и затёрла бы выбор.
     */
    private PlayerBastion saveWithNewVersion(PlayerBastion bastion) {
        entityManager.lock(bastion, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        return bastionRepository.saveAndFlush(bastion);
    }

    /**
     * Проверяет, что новый уровень персонажа не меньше уже выбранного: число
     * специализированных сооружений влезает в лимит, и каждое доступно на этом уровне.
     * Иначе мастер, понизив уровень, оставил бы персонажа с выбором не по правилам.
     *
     * @throws ApiException 400 с объяснением
     */
    public void requireLevelFits(PlayerBastionMember member, int characterLevel) {
        Map<String, BastionFacility> reference = loadReference(
                member.getFacilities().stream().map(PlayerBastionFacility::getFacilityUrl));
        List<BastionFacility> special = member.getFacilities().stream()
                .map(facility -> reference.get(facility.getFacilityUrl()))
                .filter(facility -> facility != null && facility.getCategory() == FacilityCategory.SPECIAL)
                .toList();

        int limit = BastionRules.specialFacilityLimit(characterLevel);
        if (special.size() > limit) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "У персонажа «%s» уже выбрано специализированных сооружений: %d, а на %d уровне положено %d"
                            .formatted(member.getCharacterName(), special.size(), characterLevel, limit));
        }
        special.stream()
                .filter(facility -> Optional.ofNullable(facility.getLevel()).orElse(0L) > characterLevel)
                .findFirst()
                .ifPresent(facility -> {
                    throw new ApiException(HttpStatus.BAD_REQUEST,
                            "У персонажа «%s» выбрано «%s», доступное с %d уровня"
                                    .formatted(member.getCharacterName(), facility.getName(), facility.getLevel()));
                });
    }

    /**
     * Пересобирает сооружения персонажа. Подтверждение требования сохраняется за
     * сооружением, которое осталось в выборе: переставлять порядок или менять выборы
     * сада мастеру заново подтверждать не нужно.
     */
    private static void replaceFacilities(PlayerBastionMember member,
                                          FacilitySetupRequest request,
                                          Map<String, BastionFacility> reference) {
        Map<String, Deque<PlayerBastionFacility>> previous = new HashMap<>();
        member.getFacilities().forEach(facility -> previous
                .computeIfAbsent(facility.getFacilityUrl(), url -> new ArrayDeque<>())
                .add(facility));

        List<PlayerBastionFacility> next = new ArrayList<>();
        for (FacilitySetupRequest.Basic basic : request.basic()) {
            next.add(reuseOrCreate(member, previous, basic.facilityUrl(), basic.space(), List.of(), true));
        }
        for (FacilitySetupRequest.Special special : request.special()) {
            BastionFacility facility = reference.get(special.facilityUrl());
            boolean noPrerequisite = facility.getPrerequisite() == null;
            next.add(reuseOrCreate(member, previous, special.facilityUrl(), facility.getSpace(),
                    Optional.ofNullable(special.choices()).orElse(List.of()), noPrerequisite));
        }

        member.getFacilities().clear();
        member.getFacilities().addAll(next);
    }

    private static PlayerBastionFacility reuseOrCreate(PlayerBastionMember member,
                                                       Map<String, Deque<PlayerBastionFacility>> previous,
                                                       String url,
                                                       FacilitySpace space,
                                                       List<SelectedChoice> choices,
                                                       boolean noPrerequisite) {
        PlayerBastionFacility facility = Optional.ofNullable(previous.get(url))
                .map(Deque::pollFirst)
                .orElseGet(() -> {
                    PlayerBastionFacility created = new PlayerBastionFacility();
                    created.setMember(member);
                    created.setFacilityUrl(url);
                    created.setPrerequisiteConfirmed(noPrerequisite);
                    return created;
                });
        facility.setSpace(space);
        facility.setChoices(new ArrayList<>(choices));
        if (noPrerequisite) {
            facility.setPrerequisiteConfirmed(true);
        }
        return facility;
    }

    private Map<String, BastionFacility> loadReference(Stream<String> urls) {
        Set<String> distinct = urls.collect(Collectors.toSet());
        if (distinct.isEmpty()) {
            return Map.of();
        }
        return facilityRepository.findAllById(distinct).stream()
                .collect(Collectors.toMap(BastionFacility::getUrl, Function.identity()));
    }

    private static PlayerBastionMember findMember(PlayerBastion bastion, UUID memberId) {
        return bastion.getMembers().stream()
                .filter(member -> member.getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Персонаж %s в бастионе не найден".formatted(memberId)));
    }

}
