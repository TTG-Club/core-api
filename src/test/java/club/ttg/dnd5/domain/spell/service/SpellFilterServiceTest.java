package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.service.ClassService;
import club.ttg.dnd5.domain.filter.rest.dto.FilterKeys;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.SpellQueryRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpellFilterServiceTest {
    @Mock
    private ClassService classService;
    @Mock
    private SpellRepository spellRepository;
    @Mock
    private SourceSavedFilterService sourceSavedFilterService;
    @InjectMocks
    private SpellFilterService spellFilterService;

    @Test
    void subclassFilterValueContainsParentClassRelation() {
        Source source = new Source();
        source.setAcronym("PHB");
        CharacterClass subclass = new CharacterClass();
        subclass.setUrl("arcane-trickster");
        subclass.setName("Arcane Trickster");
        subclass.setParentUrl("rogue");
        subclass.setSource(source);

        when(sourceSavedFilterService.getSavedSources()).thenReturn(Set.of("PHB"));
        when(sourceSavedFilterService.getDefaultFilterInfo(anyList(), anySet())).thenReturn(null);
        when(classService.findAllMagicClasses()).thenReturn(List.of());
        when(classService.findAllMagicSubclasses()).thenReturn(List.of(subclass));
        when(spellRepository.findAllUsedSourceCodes()).thenReturn(List.of());
        when(spellRepository.findAllUsedDistanceIds(anySet())).thenReturn(List.of());
        when(spellRepository.findDistinctSrdVersions()).thenReturn(List.of());

        var metadata = spellFilterService.getFilterMetadata(Set.of());
        String subclassKey = FilterKeys.keyOf(SpellQueryRequest.class, "subclassName");
        String classKey = FilterKeys.keyOf(SpellQueryRequest.class, "className");
        var subclassGroup = metadata.getFilters().stream()
                .filter(group -> subclassKey.equals(group.getKey()))
                .findFirst()
                .orElseThrow();

        assertEquals(Map.of(classKey, List.of("rogue")),
                subclassGroup.getValues().getFirst().getRelations());
    }
}
