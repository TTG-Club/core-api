package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.common.rest.dto.EquipmentItemDto;
import club.ttg.dnd5.domain.common.rest.dto.EquipmentOptionDto;
import club.ttg.dnd5.domain.item.repository.ItemNameRef;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.domain.magic.repository.MagicItemRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EquipmentNameResolverTest {
    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final MagicItemRepository magicItemRepository = mock(MagicItemRepository.class);
    private final EquipmentNameResolver resolver =
            new EquipmentNameResolver(itemRepository, magicItemRepository);

    @Test
    void actualNameFromCatalogWinsOverSnapshot() {
        var renamed = new EquipmentItemDto("dagger", "Название на момент сохранения", 2, null);
        var missing = new EquipmentItemDto("lost-item", "Снимок", 1, null);
        List<ItemNameRef> found = List.of(itemName("dagger", "Кинжал"));

        when(itemRepository.findNamesByUrls(Set.of("dagger", "lost-item"))).thenReturn(found);

        resolver.resolveNames(List.of(new EquipmentOptionDto("А", List.of(renamed, missing), 15, "зм")));

        assertEquals("Кинжал", renamed.getName());
        // Предмета уже нет в справочнике — остаётся название из снимка.
        assertEquals("Снимок", missing.getName());
    }

    @Test
    void optionWithoutItemsDoesNotHitRepository() {
        resolver.resolveNames(List.of(new EquipmentOptionDto("А", List.of(), 75, "зм")));

        verifyNoInteractions(itemRepository);
        verifyNoInteractions(magicItemRepository);
    }

    /**
     * Слаг магического предмета: в справочнике обычных его нет, и второй запрос идёт
     * туда. Инвентарь существа собирают из обоих справочников сразу.
     */
    @Test
    void magicItemNameResolvedBySecondLookup() {
        var magic = new EquipmentItemDto("flame-tongue", "Снимок", 1, null);

        List<ItemNameRef> fromMagic = List.of(itemName("flame-tongue", "Огненный язык"));

        when(itemRepository.findNamesByUrls(Set.of("flame-tongue"))).thenReturn(List.of());
        when(magicItemRepository.findNamesByUrls(Set.of("flame-tongue"))).thenReturn(fromMagic);

        resolver.resolveItemNames(List.of(magic));

        assertEquals("Огненный язык", magic.getName());
    }

    /** Все слаги нашлись в обычных предметах — во второй справочник не ходим. */
    @Test
    void skipsMagicLookupWhenEverythingFound() {
        var dagger = new EquipmentItemDto("dagger", "Снимок", 1, null);

        List<ItemNameRef> found = List.of(itemName("dagger", "Кинжал"));

        when(itemRepository.findNamesByUrls(Set.of("dagger"))).thenReturn(found);

        resolver.resolveItemNames(List.of(dagger));

        assertEquals("Кинжал", dagger.getName());
        verifyNoInteractions(magicItemRepository);
    }

    private ItemNameRef itemName(String url, String name) {
        ItemNameRef ref = mock(ItemNameRef.class);
        when(ref.getUrl()).thenReturn(url);
        when(ref.getName()).thenReturn(name);
        return ref;
    }
}
