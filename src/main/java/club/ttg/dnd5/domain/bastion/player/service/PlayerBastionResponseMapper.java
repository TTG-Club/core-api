package club.ttg.dnd5.domain.bastion.player.service;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.BastionRules;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembership;
import club.ttg.dnd5.domain.bastion.player.client.GameRole;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionFacility;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionMember;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionResponse;
import club.ttg.dnd5.domain.bastion.repository.BastionFacilityRepository;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionLabel;
import club.ttg.dnd5.domain.bastion.rest.dto.FacilitySpaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Собирает ответ о бастионе глазами открывшего: права, персонажи и их сооружения вместе
 * с данными справочника. Справочник читается одним запросом на все бастионы ответа.
 */
@Component
@RequiredArgsConstructor
public class PlayerBastionResponseMapper {
    private final BastionFacilityRepository facilityRepository;

    public PlayerBastionResponse toResponse(PlayerBastion bastion, GameMembership membership, UUID userId) {
        return toResponses(List.of(bastion), membership, userId).getFirst();
    }

    public List<PlayerBastionResponse> toResponses(Collection<PlayerBastion> bastions,
                                                   GameMembership membership,
                                                   UUID userId) {
        Set<String> urls = bastions.stream()
                .flatMap(bastion -> bastion.getMembers().stream())
                .flatMap(member -> member.getFacilities().stream())
                .map(PlayerBastionFacility::getFacilityUrl)
                .collect(Collectors.toSet());
        Map<String, BastionFacility> reference = urls.isEmpty()
                ? Map.of()
                : facilityRepository.findAllById(urls).stream()
                        .collect(Collectors.toMap(BastionFacility::getUrl, Function.identity()));

        return bastions.stream()
                .map(bastion -> toResponse(bastion, membership, userId, reference))
                .toList();
    }

    private PlayerBastionResponse toResponse(PlayerBastion bastion,
                                             GameMembership membership,
                                             UUID userId,
                                             Map<String, BastionFacility> reference) {
        boolean master = membership.role() == GameRole.MASTER;
        boolean archived = bastion.getStatus() == PlayerBastionStatus.ARCHIVED;
        boolean setup = bastion.getStatus() == PlayerBastionStatus.SETUP;
        boolean member = bastion.getMembers().stream().anyMatch(item -> item.getUserId().equals(userId));

        List<PlayerBastionResponse.Member> members = bastion.getMembers().stream()
                .map(item -> toMember(item, setup && (master || item.getUserId().equals(userId)),
                        bastion.getStatus() == PlayerBastionStatus.ACTIVE && (master || item.getUserId().equals(userId)),
                        reference))
                .toList();

        return new PlayerBastionResponse(
                bastion.getId(),
                bastion.getGameId(),
                bastion.getName(),
                bastion.getStatus(),
                bastion.getTurn(),
                bastion.getTreasuryGp(),
                bastion.getVersion(),
                master && !archived,
                (master || member) && !archived,
                members,
                bastion.getCreatedAt(),
                bastion.getUpdatedAt());
    }

    private static PlayerBastionResponse.Member toMember(PlayerBastionMember member,
                                                         boolean canEditFacilities,
                                                         boolean canGiveOrders,
                                                         Map<String, BastionFacility> reference) {
        List<PlayerBastionResponse.Facility> facilities = member.getFacilities().stream()
                .map(facility -> toFacility(facility, reference.get(facility.getFacilityUrl())))
                .toList();
        boolean basicComplete = facilities.stream()
                .filter(facility -> facility.category() != null
                        && FacilityCategory.BASIC.name().equals(facility.category().value()))
                .map(facility -> facility.space().value())
                .collect(Collectors.toSet())
                .size() == PlayerBastionSetupRules.STARTING_BASIC_SPACES.size();

        return new PlayerBastionResponse.Member(
                member.getId(),
                member.getUserId(),
                member.getCharacterName(),
                member.getCharacterLevel(),
                member.getCharacterSheetId(),
                BastionRules.specialFacilityLimit(member.getCharacterLevel()),
                canEditFacilities,
                basicComplete,
                canGiveOrders,
                facilities);
    }

    private static PlayerBastionResponse.Facility toFacility(PlayerBastionFacility facility, BastionFacility reference) {
        Optional<BastionFacility> source = Optional.ofNullable(reference);
        return new PlayerBastionResponse.Facility(
                facility.getId(),
                facility.getFacilityUrl(),
                source.map(BastionFacility::getName).orElse(facility.getFacilityUrl()),
                source.map(BastionFacility::getEnglish).orElse(null),
                source.map(BastionFacility::getCategory)
                        .map(category -> new BastionLabel(category.name(), category.getName()))
                        .orElse(null),
                new FacilitySpaceResponse(facility.getSpace().name(), facility.getSpace().getName(),
                        facility.getSpace().getSquares()),
                source.map(BastionFacility::getLevel).orElse(null),
                source.map(BastionFacility::getHirelings).orElse(null),
                source.map(BastionFacility::getOrders).orElse(List.of()).stream()
                        .map(order -> new BastionLabel(order.name(), order.getName()))
                        .toList(),
                source.map(BastionFacility::getPrerequisite)
                        .map(prerequisite -> new BastionLabel(prerequisite.name(), prerequisite.getName()))
                        .orElse(null),
                facility.isPrerequisiteConfirmed(),
                facility.getChoices(),
                facility.getStatus(),
                facility.getReadyOnTurn(),
                Optional.ofNullable(facility.getPendingSpace())
                        .map(space -> new FacilitySpaceResponse(space.name(), space.getName(), space.getSquares()))
                        .orElse(null),
                facility.getPendingReadyOnTurn(),
                isEnlargeable(facility, reference));
    }

    /**
     * Можно ли расширить сооружение сейчас. Базовое — до следующего пространства по
     * правилам, специализированное — если его описание допускает расширение и оно ещё
     * не сделано.
     */
    private static boolean isEnlargeable(PlayerBastionFacility facility, BastionFacility reference) {
        if (reference == null || !facility.isReady() || facility.getPendingSpace() != null) {
            return false;
        }
        if (reference.getCategory() == FacilityCategory.BASIC) {
            return facility.getSpace().next() != null;
        }
        return reference.getEnlargement() != null
                && reference.getEnlargement().getSpace() != null
                && reference.getEnlargement().getSpace() != facility.getSpace();
    }
}
