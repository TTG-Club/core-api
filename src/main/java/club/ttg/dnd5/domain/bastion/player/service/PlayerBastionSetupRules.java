package club.ttg.dnd5.domain.bastion.player.service;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.BastionRules;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilityChoice;
import club.ttg.dnd5.domain.bastion.model.FacilityChoiceOption;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.model.SelectedChoice;
import club.ttg.dnd5.domain.bastion.player.rest.dto.FacilitySetupRequest;
import club.ttg.dnd5.exception.ApiException;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Правила стартового выбора сооружений персонажа (глава «Бастионы»):
 * <ul>
 *     <li>базовые — одно тесное и одно вместительное, бесплатно; одинаковые разрешены;</li>
 *     <li>специализированные — не больше, чем положено по уровню персонажа, уровень
 *     сооружения не выше уровня персонажа, каждое — один раз, если не помечено
 *     «можно несколько»;</li>
 *     <li>выборы сооружения — только из вариантов справочника и не больше положенного.</li>
 * </ul>
 * Выбор можно сохранять частично (черновиком): проверяется, что правила не нарушены, а
 * не то, что всё уже выбрано.
 */
@UtilityClass
public class PlayerBastionSetupRules {
    /** Пространства стартовых базовых сооружений: одно тесное, одно вместительное. */
    public static final Set<FacilitySpace> STARTING_BASIC_SPACES = Set.copyOf(BastionRules.STARTING_BASIC_FACILITIES);

    /**
     * Проверяет выбор персонажа.
     *
     * @param characterLevel уровень персонажа
     * @param request        выбранные сооружения
     * @param reference      сооружения справочника по url
     * @throws ApiException 400 с объяснением, какое правило нарушено
     */
    public static void validate(int characterLevel,
                                FacilitySetupRequest request,
                                Map<String, BastionFacility> reference) {
        validateBasic(request.basic(), reference);
        validateSpecial(characterLevel, request.special(), reference);
    }

    private static void validateBasic(List<FacilitySetupRequest.Basic> basic, Map<String, BastionFacility> reference) {
        if (basic.size() > STARTING_BASIC_SPACES.size()) {
            throw badRequest("На старте у персонажа два базовых сооружения: тесное и вместительное");
        }
        Set<FacilitySpace> spaces = new HashSet<>();
        for (FacilitySetupRequest.Basic selection : basic) {
            BastionFacility facility = find(selection.facilityUrl(), reference);
            if (facility.getCategory() != FacilityCategory.BASIC) {
                throw badRequest("«%s» — не базовое сооружение".formatted(facility.getName()));
            }
            if (!STARTING_BASIC_SPACES.contains(selection.space())) {
                throw badRequest("Стартовое базовое сооружение может быть только тесным или вместительным");
            }
            if (!spaces.add(selection.space())) {
                throw badRequest("На старте одно базовое сооружение тесное, а другое — вместительное");
            }
        }
    }

    /**
     * Проверяет специализированные сооружения персонажа: лимит по уровню, уровень
     * сооружения, повторы и выборы. Нужна и стартовому выбору, и новому сооружению,
     * полученному с уровнем, когда бастион уже запущен.
     */
    public static void validateSpecial(int characterLevel,
                                        List<FacilitySetupRequest.Special> special,
                                        Map<String, BastionFacility> reference) {
        int limit = BastionRules.specialFacilityLimit(characterLevel);
        if (special.size() > limit) {
            throw badRequest("Персонажу %d уровня положено специализированных сооружений: %d"
                    .formatted(characterLevel, limit));
        }
        Set<String> taken = new HashSet<>();
        for (FacilitySetupRequest.Special selection : special) {
            BastionFacility facility = find(selection.facilityUrl(), reference);
            if (facility.getCategory() != FacilityCategory.SPECIAL) {
                throw badRequest("«%s» — не специализированное сооружение".formatted(facility.getName()));
            }
            long facilityLevel = Optional.ofNullable(facility.getLevel()).orElse(0L);
            if (facilityLevel > characterLevel) {
                throw badRequest("«%s» доступно с %d уровня".formatted(facility.getName(), facilityLevel));
            }
            if (!taken.add(facility.getUrl()) && !Boolean.TRUE.equals(facility.getRepeatable())) {
                throw badRequest("«%s» можно взять только один раз".formatted(facility.getName()));
            }
            validateChoices(facility, Optional.ofNullable(selection.choices()).orElse(List.of()));
        }
    }

    private static void validateChoices(BastionFacility facility, List<SelectedChoice> selected) {
        Map<String, FacilityChoice> available = Optional.ofNullable(facility.getChoices()).orElse(List.of()).stream()
                .filter(choice -> choice.getName() != null)
                .collect(Collectors.toMap(FacilityChoice::getName, choice -> choice, (first, second) -> first));
        Set<String> seen = new HashSet<>();
        for (SelectedChoice choice : selected) {
            FacilityChoice definition = available.get(choice.name());
            if (definition == null || !seen.add(choice.name())) {
                throw badRequest("У «%s» нет выбора «%s»".formatted(facility.getName(), choice.name()));
            }
            List<String> options = Optional.ofNullable(choice.options()).orElse(List.of());
            Set<String> allowed = Optional.ofNullable(definition.getOptions()).orElse(List.of()).stream()
                    .map(FacilityChoiceOption::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!allowed.containsAll(options) || new HashSet<>(options).size() != options.size()) {
                throw badRequest("В выборе «%s» есть вариант, которого нет в справочнике".formatted(choice.name()));
            }
            int count = Optional.ofNullable(definition.getCount()).orElse(1);
            if (options.size() > count) {
                throw badRequest("В выборе «%s» можно взять вариантов: %d".formatted(choice.name(), count));
            }
        }
    }

    private static BastionFacility find(String url, Map<String, BastionFacility> reference) {
        BastionFacility facility = reference.get(url);
        if (facility == null || facility.isHiddenEntity()) {
            throw badRequest("Сооружения %s нет в справочнике".formatted(url));
        }
        return facility;
    }

    private static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }
}
