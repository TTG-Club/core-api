package club.ttg.dnd5.domain.bastion.player.service;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.BastionRules;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilityChoice;
import club.ttg.dnd5.domain.bastion.model.FacilityChoiceOption;
import club.ttg.dnd5.domain.bastion.model.MenagerieBeast;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionEditor;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionFacility;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionMember;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionEditorRepository;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionResponse;
import club.ttg.dnd5.domain.bastion.repository.BastionFacilityRepository;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionLabel;
import club.ttg.dnd5.domain.bastion.rest.dto.FacilitySpaceResponse;
import club.ttg.dnd5.domain.bastion.service.FacilityChoiceOptions;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.common.model.EntityRef;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final PlayerBastionEditorRepository editorRepository;
    private final FacilityChoiceOptions choiceOptions;

    public PlayerBastionResponse toResponse(PlayerBastion bastion, BastionViewer viewer) {
        return toResponses(List.of(bastion), item -> viewer).getFirst();
    }

    /**
     * Ответы о нескольких бастионах.
     *
     * @param viewerOf права открывшего на каждый бастион
     */
    public List<PlayerBastionResponse> toResponses(Collection<PlayerBastion> bastions,
                                                   Function<PlayerBastion, BastionViewer> viewerOf) {
        Set<String> urls = bastions.stream()
                .flatMap(bastion -> bastion.getMembers().stream())
                .flatMap(member -> member.getFacilities().stream())
                .map(PlayerBastionFacility::getFacilityUrl)
                .collect(Collectors.toSet());
        Map<String, BastionFacility> reference = urls.isEmpty()
                ? Map.of()
                : facilityRepository.findAllById(urls).stream()
                        .collect(Collectors.toMap(BastionFacility::getUrl, Function.identity()));

        Function<FacilityChoice, List<FacilityChoiceOption>> options = choiceOptions.resolver();
        return bastions.stream()
                .map(bastion -> toResponse(bastion, viewerOf.apply(bastion), reference, options))
                .toList();
    }

    private PlayerBastionResponse toResponse(PlayerBastion bastion,
                                             BastionViewer viewer,
                                             Map<String, BastionFacility> reference,
                                             Function<FacilityChoice, List<FacilityChoiceOption>> options) {
        boolean manager = viewer.manager();
        boolean archived = bastion.getStatus() == PlayerBastionStatus.ARCHIVED;
        boolean setup = bastion.getStatus() == PlayerBastionStatus.SETUP;

        List<PlayerBastionResponse.Member> members = bastion.getMembers().stream()
                .map(item -> toMember(item, setup && (manager || viewer.is(item.getUserId())),
                        bastion.getStatus() == PlayerBastionStatus.ACTIVE && (manager || viewer.is(item.getUserId())),
                        reference, options))
                .toList();

        return new PlayerBastionResponse(
                bastion.getId(),
                bastion.getGameId(),
                bastion.getOwnerId(),
                bastion.getName(),
                bastion.getStatus(),
                bastion.getTurn(),
                bastion.getTreasuryGp(),
                bastion.getVersion(),
                viewer.owner(),
                manager && !archived,
                viewer.canEdit() && !archived,
                bastion.getGameId() != null && (viewer.owner() || manager),
                viewer.owner() ? bastion.getShareToken() : null,
                viewer.owner() && bastion.getShareToken() != null
                        ? editorRepository.countByBastionIdAndStatus(bastion.getId(), PlayerBastionEditor.Status.PENDING)
                        : 0,
                viewer.editor(),
                members,
                bastion.getCreatedAt(),
                bastion.getUpdatedAt());
    }

    private PlayerBastionResponse.Member toMember(PlayerBastionMember member,
                                                  boolean canEditFacilities,
                                                  boolean canGiveOrders,
                                                  Map<String, BastionFacility> reference,
                                                  Function<FacilityChoice, List<FacilityChoiceOption>> options) {
        List<PlayerBastionResponse.Facility> facilities = member.getFacilities().stream()
                .map(facility -> toFacility(facility, reference.get(facility.getFacilityUrl()), options))
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

    private PlayerBastionResponse.Facility toFacility(PlayerBastionFacility facility,
                                                      BastionFacility reference,
                                                      Function<FacilityChoice, List<FacilityChoiceOption>> options) {
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
                isEnlargeable(facility, reference),
                isBarrack(reference) ? facility.getDefenders() : null,
                isBarrack(reference) ? BastionRules.barrackCapacity(facility.getSpace()) : null,
                craftableItems(facility, reference, options),
                isMenagerie(reference) ? menagerie(facility) : null);
    }

    /** Сооружение справочника — зверинец, в вольерах живут существа. */
    public static boolean isMenagerie(BastionFacility reference) {
        return reference != null && Boolean.TRUE.equals(reference.getMenagerie());
    }

    /** Сколько места в вольерах занимают существа зверинца, в четвертях большого. */
    public static int menagerieOccupied(PlayerBastionFacility facility) {
        return Optional.ofNullable(facility.getCreatures()).orElse(List.of()).stream()
                .mapToInt(creature -> BastionRules.menagerieSpace(creature.getSize()))
                .sum();
    }

    private static PlayerBastionResponse.Menagerie menagerie(PlayerBastionFacility facility) {
        List<PlayerBastionResponse.Creature> creatures = Optional.ofNullable(facility.getCreatures())
                .orElse(List.of())
                .stream()
                .map(creature -> new PlayerBastionResponse.Creature(creature.getId(), creature.getName(),
                        sizeLabel(creature.getSize()), creature.getChallenge(),
                        BastionRules.menagerieSpace(creature.getSize()), creature.isDefender()))
                .toList();
        List<PlayerBastionResponse.Beast> beasts = Arrays.stream(MenagerieBeast.values())
                .map(beast -> new PlayerBastionResponse.Beast(beast.name(), beast.getName(),
                        sizeLabel(beast.getSize()), BastionRules.menagerieSpace(beast.getSize()), beast.getCost()))
                .toList();
        return new PlayerBastionResponse.Menagerie(BastionRules.MENAGERIE_CAPACITY, menagerieOccupied(facility),
                creatures, beasts, BastionRules.MENAGERIE_CHALLENGE_COSTS);
    }

    private static BastionLabel sizeLabel(Size size) {
        return size == null ? null : new BastionLabel(size.name(), size.getNames()[0]);
    }

    /**
     * Что сооружение может изготовить выбранными инструментами (мастерская), с ценой и
     * сроком изготовления силами наёмников сооружения.
     *
     * @return предметы; {@code null}, если у сооружения нет выбора инструментов
     */
    public List<PlayerBastionResponse.CraftableItem> craftableItems(PlayerBastionFacility facility,
                                                                    BastionFacility reference) {
        return craftableItems(facility, reference, choiceOptions.resolver());
    }

    private List<PlayerBastionResponse.CraftableItem> craftableItems(
            PlayerBastionFacility facility,
            BastionFacility reference,
            Function<FacilityChoice, List<FacilityChoiceOption>> options) {
        List<EntityRef> crafts = reference == null
                ? null
                : choiceOptions.crafts(reference.getChoices(), facility.getChoices(), options);
        if (crafts == null) {
            return null;
        }
        Map<String, Double> prices = choiceOptions.pricesGp(crafts.stream().map(EntityRef::getUrl).toList());
        return crafts.stream()
                .map(item -> {
                    Double price = prices.get(item.getUrl());
                    return new PlayerBastionResponse.CraftableItem(item.getUrl(), item.getName(), price,
                            price == null ? null : BastionRules.craftDays(price, reference.getHirelings()));
                })
                .toList();
    }

    /** Сооружение справочника — барак, в нём живут защитники. */
    public static boolean isBarrack(BastionFacility reference) {
        return reference != null && Boolean.TRUE.equals(reference.getBarrack());
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
