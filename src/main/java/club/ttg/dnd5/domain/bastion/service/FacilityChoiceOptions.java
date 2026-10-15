package club.ttg.dnd5.domain.bastion.service;

import club.ttg.dnd5.domain.bastion.model.FacilityChoice;
import club.ttg.dnd5.domain.bastion.model.FacilityChoiceOption;
import club.ttg.dnd5.domain.bastion.model.FacilityChoiceSource;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.model.tool.Tool;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Варианты выбора сооружения, которые берутся из справочника, а не перечисляются в
 * сооружении: инструменты мастерской — это инструменты ремесленников из раздела предметов
 * вместе с тем, что ими можно создать.
 */
@Component
@RequiredArgsConstructor
public class FacilityChoiceOptions {
    private static final String CRAFTS_PREFIX = "Создаёт: ";

    private final ItemRepository itemRepository;

    /**
     * Резолвер вариантов на один запрос: каждый источник читается из базы один раз,
     * сколько бы сооружений его ни использовали.
     *
     * @return варианты выбора — из источника, если он задан, иначе из самого выбора
     */
    public Function<FacilityChoice, List<FacilityChoiceOption>> resolver() {
        Map<FacilityChoiceSource, List<FacilityChoiceOption>> loaded = new EnumMap<>(FacilityChoiceSource.class);
        return choice -> choice.getSource() == null
                ? choice.getOptions()
                : loaded.computeIfAbsent(choice.getSource(), this::load);
    }

    /**
     * Копии выборов, у которых варианты источника подставлены вместо списка, — для ответа.
     * Сами выборы не меняются: это поле сущности.
     */
    public List<FacilityChoice> resolve(List<FacilityChoice> choices,
                                        Function<FacilityChoice, List<FacilityChoiceOption>> resolver) {
        if (choices == null || choices.stream().noneMatch(choice -> choice != null && choice.getSource() != null)) {
            return choices;
        }
        return choices.stream()
                .map(choice -> choice == null || choice.getSource() == null ? choice : withOptions(choice, resolver))
                .toList();
    }

    private List<FacilityChoiceOption> load(FacilityChoiceSource source) {
        return switch (source) {
            case ARTISAN_TOOLS -> itemRepository.findArtisanTools().stream()
                    .map(FacilityChoiceOptions::toOption)
                    .toList();
        };
    }

    private static FacilityChoice withOptions(FacilityChoice choice,
                                              Function<FacilityChoice, List<FacilityChoiceOption>> resolver) {
        FacilityChoice copy = new FacilityChoice();
        copy.setName(choice.getName());
        copy.setDescription(choice.getDescription());
        copy.setCount(choice.getCount());
        copy.setEnlargedCount(choice.getEnlargedCount());
        copy.setSource(choice.getSource());
        copy.setOptions(resolver.apply(choice));
        return copy;
    }

    private static FacilityChoiceOption toOption(Item tool) {
        List<EntityRef> crafts = Optional.ofNullable(tool.getTool())
                .map(Tool::getCrafts)
                .orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .toList();
        FacilityChoiceOption option = new FacilityChoiceOption();
        option.setName(tool.getName());
        option.setUrl(tool.getUrl());
        option.setCrafts(crafts);
        String names = crafts.stream()
                .map(EntityRef::getName)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.joining(", "));
        option.setDescription(names.isEmpty() ? null : CRAFTS_PREFIX + names);
        return option;
    }
}
