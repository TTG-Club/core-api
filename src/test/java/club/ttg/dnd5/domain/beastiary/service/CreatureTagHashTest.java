package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.dto.base.filters.FilterIdUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreatureTagHashTest
{
    @Test
    void tagsAreSplitTrimmedAndKeyedByTheHashTheFilterListGives()
    {
        Map<String, String> tagsByHash = CreatureServiceImpl.tagsByComputedHash(
                Arrays.asList("вампир", "перевёртыш, вампир", " колдун ", "", null));

        assertEquals(Map.of(
                FilterIdUtils.shortHash("вампир"), "вампир",
                FilterIdUtils.shortHash("перевёртыш"), "перевёртыш",
                FilterIdUtils.shortHash("колдун"), "колдун"), tagsByHash);
    }

    @Test
    void hashOfVampireTagMatchesTheIdShownInFilters()
    {
        // Хэш, который список фильтра бестиария отдаёт для тега «вампир»
        assertEquals(List.of("197cb7ea"),
                List.copyOf(CreatureServiceImpl.tagsByComputedHash(List.of("вампир")).keySet()));
    }
}
