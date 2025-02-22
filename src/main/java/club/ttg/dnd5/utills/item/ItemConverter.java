package club.ttg.dnd5.utills.item;

import club.ttg.dnd5.dictionary.item.ItemType;
import club.ttg.dnd5.dictionary.item.magic.Rarity;
import club.ttg.dnd5.dto.NameDto;
import club.ttg.dnd5.dto.item.ItemDto;
import club.ttg.dnd5.model.item.Item;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.BiFunction;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemConverter {
    public static final BiFunction<ItemDto, Item, Item> MAP_DTO_TO_ENTITY = (dto, entity) -> {
        entity.setTypes(dto.getTypes()
                .stream()
                .map(t -> ItemType.valueOf(t.getEng()))
                .collect(Collectors.toSet()));
        entity.setCost(dto.getCost());
        entity.setWeight(dto.getWeight());

        entity.setMagic(dto.isMagic());
        if (dto.isMagic()) {
            entity.setAttunement(dto.getAttunement());
            entity.setRarity(Rarity.parse(dto.getRarity().getEng()));
            entity.setCharges(dto.getCharges());
            entity.setTypeClarification(dto.getTypeClarification());
        }
        return entity;
    };

    public static final BiFunction<ItemDto, Item, ItemDto> MAP_ENTITY_TO_DTO_ = (dto, entity) -> {
        dto.setTypes(entity.getTypes()
                .stream().map(t -> NameDto.builder()
                        .rus(t.getName())
                        .eng(t.name())
                        .build())
                .collect(Collectors.toSet()));
        dto.setCost(entity.getCost());
        dto.setWeight(dto.getWeight());
        if (entity.isMagic()) {
            dto.setRarity(NameDto.builder()
                    .rus(entity.getRarity().getName())
                    .eng(entity.getRarity().name())
                    .build());
            dto.setAttunement(entity.getAttunement());
            dto.setCharges(entity.getCharges());
            dto.setTypeClarification(entity.getTypeClarification());
        }
        return dto;
    };
}
