package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.filter.repository.FilterHashMappingRepository;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Группы фильтра бестиария по уязвимостям, сопротивлениям и иммунитетам — как на 5e14:
 * ключи запроса, названия, полный набор значений и поддержка «Исключать» и «совпадения всех».
 */
@ExtendWith(MockitoExtension.class)
class CreatureFilterServiceTest
{
    @Mock
    private CreatureRepository creatureRepository;
    @Mock
    private SourceSavedFilterService sourceSavedFilterService;
    @Mock
    private FilterHashMappingRepository filterHashMappingRepository;
    @InjectMocks
    private CreatureFilterService creatureFilterService;

    private Map<String, FilterGroupMeta> groups;
    private List<String> keys;

    @BeforeEach
    void setUp()
    {
        when(filterHashMappingRepository.findAllByCategory(any())).thenReturn(List.of());
        when(creatureRepository.findDistinctTags()).thenReturn(List.of());
        when(creatureRepository.findDistinctSrdVersions()).thenReturn(List.of());
        when(creatureRepository.findAllUsedSourceCodes()).thenReturn(List.of());

        List<FilterGroupMeta> filters = creatureFilterService.getFilterMetadata(Set.of()).getFilters();
        keys = filters.stream().map(FilterGroupMeta::getKey).toList();
        groups = filters.stream().collect(Collectors.toMap(FilterGroupMeta::getKey, Function.identity()));
    }

    private void assertGroup(final String key, final String name, final Map<String, String> expectedValues)
    {
        FilterGroupMeta group = groups.get(key);
        assertEquals(name, group.getName());
        assertTrue(group.getSupports().isMode(), key + ": «Исключать»");
        assertTrue(group.getSupports().isUnion(), key + ": совпадение всех");

        Map<String, String> values = group.getValues().stream()
                .collect(Collectors.toMap(FilterValueMeta::getId, FilterValueMeta::getName));
        assertEquals(expectedValues, values);
        group.getValues().forEach(value -> assertEquals(value.getId(), value.getValue()));
    }

    private static Map<String, String> damageTypes()
    {
        return Arrays.stream(DamageType.values())
                .collect(Collectors.toMap(DamageType::name, DamageType::getName));
    }

    @Test
    @DisplayName("уязвимость, сопротивление и иммунитет к урону — все типы урона")
    void damageGroups()
    {
        assertGroup("vulnerability", "Уязвимость к урону", damageTypes());
        assertGroup("resistance", "Сопротивление к урону", damageTypes());
        assertGroup("damageImmunity", "Иммунитет к урону", damageTypes());
    }

    @Test
    @DisplayName("иммунитет к состояниям — все состояния")
    void conditionImmunityGroup()
    {
        assertGroup("conditionImmunity", "Иммунитет к состояниям", Arrays.stream(Condition.values())
                .collect(Collectors.toMap(Condition::name, Condition::getName)));
    }

    @Test
    @DisplayName("новые группы идут подряд после чувств")
    void groupOrder()
    {
        int senses = keys.indexOf("senses");
        assertEquals(List.of("vulnerability", "resistance", "damageImmunity", "conditionImmunity"),
                keys.subList(senses + 1, senses + 5));
    }
}
