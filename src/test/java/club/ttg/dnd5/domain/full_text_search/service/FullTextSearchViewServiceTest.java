package club.ttg.dnd5.domain.full_text_search.service;

import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.full_text_search.model.FullTextSearchView;
import club.ttg.dnd5.domain.full_text_search.repository.FullTextSearchViewRepository;
import club.ttg.dnd5.domain.full_text_search.rest.dto.FullTextSearchViewDto;
import club.ttg.dnd5.domain.full_text_search.rest.dto.FullTextSearchViewResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Глобальный поиск: точное совпадение не должно выпадать за лимит «N на раздел»,
 * когда вхождений запроса в разделе больше лимита.
 */
class FullTextSearchViewServiceTest {

    private final FullTextSearchViewRepository repository = mock(FullTextSearchViewRepository.class);
    private final FullTextSearchViewService service = new FullTextSearchViewService(repository);

    {
        ReflectionTestUtils.setField(service, "maxItemsPerGroup", 5);
    }

    @Test
    void exactMatchComesFirstAndSurvivesGroupLimit() {
        when(repository.findBySearchLine(anyString(), anyString())).thenReturn(List.of(
                view("robe-of-the-archmagi", "Мантия архимага", "Robe of the Archmagi", SectionType.MAGIC_ITEM),
                view("illusionist-archmage", "Архимаг-иллюзионист", "Illusionist Archmage", SectionType.BESTIARY),
                view("necromancer-archmage", "Архимаг-некромант", "Necromancer Archmage", SectionType.BESTIARY),
                view("abjurer-archmage", "Архимаг-оградитель", "Abjurer Archmage", SectionType.BESTIARY),
                view("enchanter-archmage", "Архимаг-очарователь", "Enchanter Archmage", SectionType.BESTIARY),
                view("transmuter-archmage", "Архимаг-преобразователь", "Transmuter Archmage", SectionType.BESTIARY),
                view("archmage", "Архимаг", "Archmage", SectionType.BESTIARY)));

        FullTextSearchViewResponse response = service.findBySearchLine(" архимаг ");

        List<String> bestiary = urls(response, SectionType.BESTIARY);
        assertEquals(5, bestiary.size());
        assertEquals("archmage", bestiary.getFirst());
        assertEquals(List.of("robe-of-the-archmagi"), urls(response, SectionType.MAGIC_ITEM));
        assertEquals(7, response.getTotal());
        assertEquals(6, response.getFiltered());
    }

    @Test
    void ranksPrefixThenWordStartThenContains() {
        when(repository.findBySearchLine(anyString(), anyString())).thenReturn(List.of(
                view("contains", "Огнешар", null, SectionType.SPELL),
                view("word-start", "Большой шар", null, SectionType.SPELL),
                view("prefix", "Шаровая молния", null, SectionType.SPELL),
                view("exact", "Шар", null, SectionType.SPELL)));

        FullTextSearchViewResponse response = service.findBySearchLine("шар");

        assertEquals(List.of("exact", "prefix", "word-start", "contains"), urls(response, SectionType.SPELL));
    }

    @Test
    void matchesQueryTypedInWrongLayout() {
        when(repository.findBySearchLine(anyString(), anyString())).thenReturn(List.of(
                view("archmage-illusionist", "Архимаг-иллюзионист", null, SectionType.BESTIARY),
                view("mage", "Маг", null, SectionType.BESTIARY)));

        // «vfu» в английской раскладке — это «маг».
        FullTextSearchViewResponse response = service.findBySearchLine("vfu");

        assertEquals("mage", urls(response, SectionType.BESTIARY).getFirst());
    }

    private static List<String> urls(FullTextSearchViewResponse response, SectionType type) {
        return response.getItems().stream()
                .filter(item -> item.getType() == type)
                .map(FullTextSearchViewDto::getUrl)
                .toList();
    }

    private static FullTextSearchView view(String url, String name, String english, SectionType type) {
        FullTextSearchView view = new FullTextSearchView();
        view.setUrl(url);
        view.setName(name);
        view.setEnglish(english);
        view.setType(type);
        return view;
    }
}
