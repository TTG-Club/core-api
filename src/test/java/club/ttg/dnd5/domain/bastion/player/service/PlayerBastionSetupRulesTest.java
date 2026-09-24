package club.ttg.dnd5.domain.bastion.player.service;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilityChoice;
import club.ttg.dnd5.domain.bastion.model.FacilityChoiceOption;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.model.SelectedChoice;
import club.ttg.dnd5.domain.bastion.player.rest.dto.FacilitySetupRequest;
import club.ttg.dnd5.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerBastionSetupRulesTest {
    private final Map<String, BastionFacility> reference = Stream.of(
                    basic("bedroom"),
                    basic("kitchen"),
                    special("library", 5, false),
                    special("barrack", 5, true),
                    special("smithy", 5, false),
                    special("gaming-hall", 9, false),
                    garden())
            .collect(Collectors.toMap(BastionFacility::getUrl, Function.identity()));

    @Test
    void startingSetOfLevelFiveCharacterIsValid() {
        assertDoesNotThrow(() -> PlayerBastionSetupRules.validate(5, request(
                List.of(new FacilitySetupRequest.Basic("bedroom", FacilitySpace.CRAMPED),
                        new FacilitySetupRequest.Basic("bedroom", FacilitySpace.ROOMY)),
                List.of(special("library"), special("smithy"))), reference));
    }

    /** Черновик: часть выбора ещё не сделана — это не нарушение правил. */
    @Test
    void partialSelectionIsAllowed() {
        assertDoesNotThrow(() -> PlayerBastionSetupRules.validate(5, request(
                List.of(new FacilitySetupRequest.Basic("kitchen", FacilitySpace.ROOMY)), List.of()), reference));
    }

    @Test
    void basicMustBeOneCrampedAndOneRoomy() {
        assertRejected(5, request(
                List.of(new FacilitySetupRequest.Basic("bedroom", FacilitySpace.ROOMY),
                        new FacilitySetupRequest.Basic("kitchen", FacilitySpace.ROOMY)), List.of()),
                "тесное");
        assertRejected(5, request(
                List.of(new FacilitySetupRequest.Basic("kitchen", FacilitySpace.VAST)), List.of()),
                "тесным или вместительным");
    }

    @Test
    void specialLimitDependsOnLevel() {
        assertRejected(5, request(List.of(),
                List.of(special("library"), special("smithy"), special("barrack"))), "положено");
        assertDoesNotThrow(() -> PlayerBastionSetupRules.validate(9, request(List.of(),
                List.of(special("library"), special("smithy"), special("barrack"))), reference));
    }

    @Test
    void facilityLevelMustNotExceedCharacterLevel() {
        assertRejected(5, request(List.of(), List.of(special("gaming-hall"))), "с 9 уровня");
    }

    @Test
    void specialFacilityIsTakenOnceUnlessRepeatable() {
        assertRejected(5, request(List.of(), List.of(special("library"), special("library"))), "один раз");
        assertDoesNotThrow(() -> PlayerBastionSetupRules.validate(5,
                request(List.of(), List.of(special("barrack"), special("barrack"))), reference));
    }

    @Test
    void wrongCategoryIsRejected() {
        assertRejected(5, request(List.of(new FacilitySetupRequest.Basic("library", FacilitySpace.ROOMY)), List.of()),
                "не базовое");
        assertRejected(5, request(List.of(), List.of(special("bedroom"))), "не специализированное");
    }

    @Test
    void choicesAreCheckedAgainstReference() {
        assertDoesNotThrow(() -> PlayerBastionSetupRules.validate(5, request(List.of(), List.of(
                new FacilitySetupRequest.Special("garden",
                        List.of(new SelectedChoice("Тип сада", List.of("Травяной")))))), reference));
        assertRejected(5, request(List.of(), List.of(new FacilitySetupRequest.Special("garden",
                List.of(new SelectedChoice("Тип сада", List.of("Лунный")))))), "нет в справочнике");
        assertRejected(5, request(List.of(), List.of(new FacilitySetupRequest.Special("garden",
                List.of(new SelectedChoice("Тип сада", List.of("Травяной", "Пищевой")))))), "можно взять вариантов: 1");
    }

    private void assertRejected(int level, FacilitySetupRequest request, String messagePart) {
        ApiException error = assertThrows(ApiException.class,
                () -> PlayerBastionSetupRules.validate(level, request, reference));
        assertTrue(error.getMessage().contains(messagePart), error.getMessage());
    }

    private static FacilitySetupRequest request(List<FacilitySetupRequest.Basic> basic,
                                                List<FacilitySetupRequest.Special> special) {
        return new FacilitySetupRequest(basic, special, 0L);
    }

    private static FacilitySetupRequest.Special special(String url) {
        return new FacilitySetupRequest.Special(url, List.of());
    }

    private static BastionFacility basic(String url) {
        BastionFacility facility = new BastionFacility();
        facility.setUrl(url);
        facility.setName(url);
        facility.setCategory(FacilityCategory.BASIC);
        return facility;
    }

    private static BastionFacility special(String url, long level, boolean repeatable) {
        BastionFacility facility = new BastionFacility();
        facility.setUrl(url);
        facility.setName(url);
        facility.setCategory(FacilityCategory.SPECIAL);
        facility.setLevel(level);
        facility.setRepeatable(repeatable);
        facility.setSpace(FacilitySpace.ROOMY);
        return facility;
    }

    private static BastionFacility garden() {
        BastionFacility facility = special("garden", 5, true);
        FacilityChoice choice = new FacilityChoice();
        choice.setName("Тип сада");
        choice.setCount(1);
        choice.setOptions(Stream.of("Декоративный", "Пищевой", "Травяной", "Ядовитый")
                .map(name -> {
                    FacilityChoiceOption option = new FacilityChoiceOption();
                    option.setName(name);
                    return option;
                })
                .toList());
        facility.setChoices(List.of(choice));
        return facility;
    }
}
