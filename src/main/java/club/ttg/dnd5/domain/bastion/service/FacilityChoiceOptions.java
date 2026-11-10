package club.ttg.dnd5.domain.bastion.service;

import club.ttg.dnd5.domain.bastion.model.FacilityChoice;
import club.ttg.dnd5.domain.bastion.model.FacilityChoiceOption;
import club.ttg.dnd5.domain.bastion.model.FacilityChoiceSource;
import club.ttg.dnd5.domain.common.dictionary.Coin;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.bastion.player.model.SelectedChoice;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.model.tool.Tool;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
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

    /**
     * Что можно создать вариантами, которые игрок выбрал в выборах из справочника: предметы
     * выбранных инструментов мастерской, без повторов, в порядке выбора.
     *
     * @param choices  выборы сооружения справочника
     * @param selected сделанные выборы игрока
     * @return предметы; {@code null}, если у сооружения нет выбора из справочника
     */
    public List<EntityRef> crafts(List<FacilityChoice> choices,
                                  List<SelectedChoice> selected,
                                  Function<FacilityChoice, List<FacilityChoiceOption>> resolver) {
        List<FacilityChoice> sourced = choices == null ? List.of() : choices.stream()
                .filter(choice -> choice != null && choice.getSource() != null)
                .toList();
        if (sourced.isEmpty()) {
            return null;
        }
        Map<String, EntityRef> crafts = new LinkedHashMap<>();
        for (FacilityChoice choice : sourced) {
            List<String> picked = selectedOptions(selected, choice.getName());
            resolver.apply(choice).stream()
                    .filter(option -> picked.contains(option.getName()))
                    .flatMap(option -> Optional.ofNullable(option.getCrafts()).orElse(List.of()).stream())
                    .filter(item -> item.getUrl() != null)
                    .forEach(item -> crafts.putIfAbsent(item.getUrl(), item));
        }
        return List.copyOf(crafts.values());
    }

    /**
     * Покупная стоимость предметов справочника в зм.
     *
     * @param urls предметы
     * @return стоимость по адресу; предметов без числовой цены в ответе нет
     */
    public Map<String, Double> pricesGp(Collection<String> urls) {
        if (urls.isEmpty()) {
            return Map.of();
        }
        Map<String, Double> prices = new LinkedHashMap<>();
        for (Item item : itemRepository.findAllById(urls)) {
            Double price = priceGp(item.getCost(), item.getCoin());
            if (price != null) {
                prices.put(item.getUrl(), price);
            }
        }
        return prices;
    }

    /**
     * Цена предмета в зм: число из поля стоимости («50», «1 500», «0,5») по курсу монеты.
     * Запятая с тремя цифрами после неё — разделитель тысяч, иначе — десятичная.
     */
    static Double priceGp(String cost, Coin coin) {
        if (cost == null) {
            return null;
        }
        String number = cost.replaceAll("[\\s\u00A0]", "");
        if (number.matches("\\d{1,3}(,\\d{3})+")) {
            number = number.replace(",", "");
        }
        number = number.replace(',', '.');
        if (!number.matches("\\d+(\\.\\d+)?")) {
            return null;
        }
        float rate = coin == null ? 1f : coin.getExchangeForGold();
        return Double.parseDouble(number) * rate;
    }

    private static List<String> selectedOptions(List<SelectedChoice> selected, String name) {
        return Optional.ofNullable(selected).orElse(List.of()).stream()
                .filter(choice -> choice != null && Objects.equals(choice.name(), name))
                .flatMap(choice -> Optional.ofNullable(choice.options()).orElse(List.of()).stream())
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
