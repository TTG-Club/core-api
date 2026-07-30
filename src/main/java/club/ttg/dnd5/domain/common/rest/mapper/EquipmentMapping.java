package club.ttg.dnd5.domain.common.rest.mapper;

import club.ttg.dnd5.domain.common.dictionary.Coin;
import club.ttg.dnd5.domain.common.model.EquipmentItem;
import club.ttg.dnd5.domain.common.model.EquipmentOption;
import club.ttg.dnd5.domain.common.rest.dto.EquipmentItemDto;
import club.ttg.dnd5.domain.common.rest.dto.EquipmentOptionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Общие преобразования стартового снаряжения вариантами выбора.
 * Используется сущностями, у которых есть такое снаряжение: классами и предысториями.
 */
@Mapper(componentModel = "spring")
public interface EquipmentMapping
{
    /**
     * Метки вариантов снаряжения — выводятся из порядка вариантов, в базе не хранятся.
     */
    String EQUIPMENT_LABELS = "АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЭЮЯ";

    /**
     * Количество вариантов снаряжения, которые редактор показывает там, где снаряжение не заполнено.
     */
    int DEFAULT_EQUIPMENT_OPTIONS = 2;

    /**
     * Варианты снаряжения для формы редактора: если их нет,
     * отдаём пустые подблоки «А» и «Б», чтобы редактору было что показать.
     */
    @Named("toEquipmentForm")
    default List<EquipmentOption> toEquipmentForm(List<EquipmentOption> options)
    {
        if (!CollectionUtils.isEmpty(options))
        {
            return options;
        }

        List<EquipmentOption> defaults = new ArrayList<>(DEFAULT_EQUIPMENT_OPTIONS);
        for (int index = 0; index < DEFAULT_EQUIPMENT_OPTIONS; index++)
        {
            EquipmentOption option = new EquipmentOption();
            option.setItems(new ArrayList<>());
            defaults.add(option);
        }

        return defaults;
    }

    /**
     * Варианты снаряжения для сохранения: пустые предметы и пустые подблоки отбрасываются,
     * чтобы дефолтные подблоки редактора не попадали в базу.
     */
    @Named("toEquipmentEntities")
    default List<EquipmentOption> toEquipmentEntities(List<EquipmentOption> options)
    {
        if (CollectionUtils.isEmpty(options))
        {
            return null;
        }

        List<EquipmentOption> cleaned = new ArrayList<>(options.size());
        for (EquipmentOption option : options)
        {
            if (option == null)
            {
                continue;
            }

            option.setItems(cleanEquipmentItems(option.getItems()));

            if (!option.getItems().isEmpty() || option.getCoins() != null)
            {
                cleaned.add(option);
            }
        }

        return cleaned.isEmpty() ? null : cleaned;
    }

    private List<EquipmentItem> cleanEquipmentItems(List<EquipmentItem> items)
    {
        if (CollectionUtils.isEmpty(items))
        {
            return Collections.emptyList();
        }

        return items.stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.hasText(item.getUrl()) || StringUtils.hasText(item.getDescription()))
                .toList();
    }

    @Named("toEquipmentOptionDtos")
    default List<EquipmentOptionDto> toEquipmentOptionDtos(List<EquipmentOption> options)
    {
        if (CollectionUtils.isEmpty(options))
        {
            return Collections.emptyList();
        }

        List<EquipmentOptionDto> dtos = new ArrayList<>(options.size());
        for (int index = 0; index < options.size(); index++)
        {
            EquipmentOption option = options.get(index);
            Coin coin = option.getCoin() == null ? Coin.GC : option.getCoin();

            dtos.add(new EquipmentOptionDto(
                    equipmentLabel(index),
                    toEquipmentItemDtos(option.getItems()),
                    option.getCoins(),
                    coin.getShortName()
            ));
        }

        return dtos;
    }

    List<EquipmentItemDto> toEquipmentItemDtos(List<EquipmentItem> items);

    default String equipmentLabel(int index)
    {
        return index < EQUIPMENT_LABELS.length()
                ? String.valueOf(EQUIPMENT_LABELS.charAt(index))
                : String.valueOf(index + 1);
    }
}
