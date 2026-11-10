package club.ttg.dnd5.domain.bastion.service;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.BastionRules;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilityChoice;
import club.ttg.dnd5.domain.bastion.model.FacilityChoiceOption;
import club.ttg.dnd5.domain.bastion.model.FacilityChoiceSource;
import club.ttg.dnd5.domain.bastion.player.model.SelectedChoice;
import club.ttg.dnd5.domain.bastion.player.rest.dto.FacilitySetupRequest;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionSetupRules;
import club.ttg.dnd5.domain.common.dictionary.Coin;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.model.tool.Tool;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FacilityChoiceOptionsTest {
    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final FacilityChoiceOptions choiceOptions = new FacilityChoiceOptions(itemRepository);

    @Test
    void priceIsReadInGoldWhateverTheFormat() {
        assertEquals(50.0, FacilityChoiceOptions.priceGp("50", Coin.GC));
        assertEquals(1500.0, FacilityChoiceOptions.priceGp("1 500", Coin.GC));
        assertEquals(1500.0, FacilityChoiceOptions.priceGp("1,500", Coin.GC));
        assertEquals(0.5, FacilityChoiceOptions.priceGp("0,5", Coin.GC));
        assertEquals(0.5, FacilityChoiceOptions.priceGp("5", Coin.SC), 1e-6);
        assertNull(FacilityChoiceOptions.priceGp("—", Coin.GC));
        assertNull(FacilityChoiceOptions.priceGp(null, Coin.GC));
    }

    @Test
    void craftTakesTenGoldPerDaySharedByHirelings() {
        assertEquals(5, BastionRules.craftDays(50, 1));
        assertEquals(3, BastionRules.craftDays(50, 2));
        assertEquals(1, BastionRules.craftDays(0.5, null));
        assertEquals(8, BastionRules.craftDays(150, 2));
    }

    @Test
    void artisanToolsComeFromItemsWithTheirCrafts() {
        when(itemRepository.findArtisanTools()).thenReturn(List.of(
                tool("smiths-tools", "Инструменты кузнеца", new EntityRef("club", "Дубинка"),
                        new EntityRef("mace", "Булава")),
                tool("weavers-tools", "Инструменты ткача")));

        List<FacilityChoice> resolved = choiceOptions.resolve(List.of(toolsChoice()), choiceOptions.resolver());

        List<FacilityChoiceOption> options = resolved.getFirst().getOptions();
        assertEquals(List.of("Инструменты кузнеца", "Инструменты ткача"),
                options.stream().map(FacilityChoiceOption::getName).toList());
        assertEquals("smiths-tools", options.getFirst().getUrl());
        assertEquals("Создаёт: Дубинка, Булава", options.getFirst().getDescription());
        assertEquals(2, options.getFirst().getCrafts().size());
        assertNull(options.get(1).getDescription());
        assertEquals(6, resolved.getFirst().getCount());
    }

    @Test
    void choicesWithoutSourceStayAsIs() {
        FacilityChoice garden = new FacilityChoice();
        garden.setName("Тип сада");
        List<FacilityChoice> choices = List.of(garden);

        assertSame(choices, choiceOptions.resolve(choices, choiceOptions.resolver()));
    }

    @Test
    void sourceIsReadOncePerResolver() {
        when(itemRepository.findArtisanTools()).thenReturn(List.of());
        var resolver = choiceOptions.resolver();

        choiceOptions.resolve(List.of(toolsChoice()), resolver);
        choiceOptions.resolve(List.of(toolsChoice()), resolver);

        verify(itemRepository, times(1)).findArtisanTools();
    }

    @Test
    void workshopToolsAreValidatedAgainstItems() {
        when(itemRepository.findArtisanTools()).thenReturn(List.of(
                tool("smiths-tools", "Инструменты кузнеца"), tool("weavers-tools", "Инструменты ткача")));
        BastionFacility workshop = new BastionFacility();
        workshop.setUrl("workshop");
        workshop.setName("Мастерская");
        workshop.setCategory(FacilityCategory.SPECIAL);
        workshop.setLevel(5L);
        workshop.setChoices(List.of(toolsChoice()));
        Map<String, BastionFacility> reference = Map.of("workshop", workshop);

        assertDoesNotThrow(() -> PlayerBastionSetupRules.validateSpecial(5, List.of(workshopWith("Инструменты ткача")),
                reference, choiceOptions.resolver()));
        ApiException error = assertThrows(ApiException.class, () -> PlayerBastionSetupRules.validateSpecial(5,
                List.of(workshopWith("Инструменты пивовара")), reference, choiceOptions.resolver()));
        assertTrue(error.getMessage().contains("нет в справочнике"), error.getMessage());
    }

    private static FacilitySetupRequest.Special workshopWith(String tool) {
        return new FacilitySetupRequest.Special("workshop",
                List.of(new SelectedChoice("Инструменты ремесленников", List.of(tool))));
    }

    private static FacilityChoice toolsChoice() {
        FacilityChoice choice = new FacilityChoice();
        choice.setName("Инструменты ремесленников");
        choice.setCount(6);
        choice.setSource(FacilityChoiceSource.ARTISAN_TOOLS);
        return choice;
    }

    private static Item tool(String url, String name, EntityRef... crafts) {
        Tool tool = new Tool();
        tool.setCategory("artisan");
        tool.setCrafts(List.of(crafts));
        Item item = new Item();
        item.setUrl(url);
        item.setName(name);
        item.setTool(tool);
        return item;
    }
}
