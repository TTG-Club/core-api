package club.ttg.dnd5.domain.magic.rest.mapper;

import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class MagicItemSubtitleTest {

    @Mock
    private BaseMapping baseMapping;

    @InjectMocks
    private MagicItemMapperImpl mapper;

    @Test
    void linkedItemsAreListedByNameInAlphabeticalOrder() {
        MagicItem sword = weapon(null, "Скимитар", "Длинный меч", "Рапира");

        assertEquals("Оружие (Длинный меч, Рапира, Скимитар), редкое", mapper.toSubtitle(sword));
    }

    @Test
    void manualTextFollowsLinkedItems() {
        MagicItem sword = weapon("любой с лезвием", "Рапира");

        assertEquals("Оружие (Рапира, любой с лезвием), редкое", mapper.toSubtitle(sword));
    }

    @Test
    void manualTextAloneStaysAsIs() {
        MagicItem weapon = weapon("любое простое или воинское");

        assertEquals("Оружие (любое простое или воинское), редкое", mapper.toSubtitle(weapon));
    }

    @Test
    void nothingToClarifyLeavesNoBrackets() {
        assertEquals("Оружие, редкое", mapper.toSubtitle(weapon(null)));
    }

    @Test
    void tooManyLinkedItemsAreNotListed() {
        String[] names = IntStream.rangeClosed(1, MagicItemMapper.SUBTITLE_ITEMS_LIMIT + 1)
                .mapToObj(index -> "Оружие " + index)
                .toArray(String[]::new);

        assertEquals("Оружие, редкое", mapper.toSubtitle(weapon(null, names)));
        assertEquals("Оружие (любое рукопашное), редкое", mapper.toSubtitle(weapon("любое рукопашное", names)));
    }

    private MagicItem weapon(String clarification, String... itemNames) {
        MagicItem magicItem = new MagicItem();
        magicItem.setCategory(MagicItemCategory.WEAPON);
        magicItem.setRarity(Rarity.RARE);
        magicItem.setClarification(clarification);

        Set<Item> items = new LinkedHashSet<>();
        for (String itemName : itemNames) {
            Item item = new Item();
            item.setUrl(itemName);
            item.setName(itemName);
            items.add(item);
        }
        magicItem.setItems(items);
        return magicItem;
    }
}
