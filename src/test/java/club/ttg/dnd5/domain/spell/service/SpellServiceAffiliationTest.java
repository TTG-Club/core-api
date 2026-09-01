package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.character_class.service.ClassService;
import club.ttg.dnd5.domain.feat.service.FeatService;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.service.SpeciesService;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellAffiliationLevelView;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.create.CreateAffiliationRequest;
import club.ttg.dnd5.domain.spell.rest.dto.create.SpellRequest;
import club.ttg.dnd5.domain.spell.rest.mapper.SpellMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Связь заклинания с видами живёт в таблице, общей с врождёнными заклинаниями вида,
 * и несёт чужую колонку {@code required_level}. Сохранение заклинания обязано править
 * её построчно: уцелевшую связь трогать нельзя, иначе уровень вида сбрасывается.
 */
@ExtendWith(MockitoExtension.class)
class SpellServiceAffiliationTest
{
    @Mock
    private ClassService classService;
    @Mock
    private SpeciesService speciesService;
    @Mock
    private FeatService featService;
    @Mock
    private SourceService sourceService;
    @Mock
    private SpellRepository spellRepository;
    @Mock
    private SpellMapper spellMapper;
    @Mock
    private SpellQueryDslSearchService spellQueryDslSearchService;
    @Mock
    private SourceSavedFilterService sourceSavedFilterService;
    @Mock
    private EntityRevisionService revisionService;

    @InjectMocks
    private SpellService spellService;

    @Test
    void updateTouchesOnlyChangedSpeciesLinks()
    {
        Spell existing = new Spell();
        existing.setUrl("fire-bolt-phb");
        existing.setLevel(0L);

        when(spellRepository.findForUpdateByUrl("fire-bolt-phb")).thenReturn(Optional.of(existing));
        when(speciesService.findAllById(Set.of("flamekin-lfl", "rimekin-lfl")))
                .thenReturn(Set.of(species("flamekin-lfl"), species("rimekin-lfl")));
        when(spellRepository.findSpeciesAffiliationLevels("fire-bolt-phb"))
                .thenReturn(List.of(level("flamekin-lfl", 3), level("harpy-feq", 1)));

        spellService.update("fire-bolt-phb", request(Set.of("flamekin-lfl", "rimekin-lfl")));

        // Убрали только тот вид, которого больше нет в запросе.
        ArgumentCaptor<java.util.Collection<String>> removed = ArgumentCaptor.forClass(java.util.Collection.class);
        verify(spellRepository).deleteSpeciesAffiliations(eq("fire-bolt-phb"), removed.capture());
        assertEquals(Set.of("harpy-feq"), Set.copyOf(removed.getValue()));

        // Завели только новый; связь с уровнем 3 не переписывалась вовсе.
        verify(spellRepository).addSpeciesAffiliation("fire-bolt-phb", "rimekin-lfl", 1);
        verify(spellRepository, never()).addSpeciesAffiliation("fire-bolt-phb", "flamekin-lfl", 1);
    }

    @Test
    void updateWithoutChangesLeavesSpeciesLinksAlone()
    {
        Spell existing = new Spell();
        existing.setUrl("fire-bolt-phb");
        existing.setLevel(0L);

        when(spellRepository.findForUpdateByUrl("fire-bolt-phb")).thenReturn(Optional.of(existing));
        when(speciesService.findAllById(Set.of("flamekin-lfl")))
                .thenReturn(Set.of(species("flamekin-lfl")));
        when(spellRepository.findSpeciesAffiliationLevels("fire-bolt-phb"))
                .thenReturn(List.of(level("flamekin-lfl", 3)));

        spellService.update("fire-bolt-phb", request(Set.of("flamekin-lfl")));

        verify(spellRepository, never()).deleteSpeciesAffiliations(anyString(), any());
        verify(spellRepository, never()).addSpeciesAffiliation(anyString(), anyString(), anyInt());
    }

    private SpellRequest request(Set<String> speciesUrls)
    {
        SpellRequest request = new SpellRequest();
        request.setUrl("fire-bolt-phb");
        request.setLevel(0L);
        request.setAffiliations(CreateAffiliationRequest.builder().species(speciesUrls).build());
        return request;
    }

    private Species species(String url)
    {
        Species species = new Species();
        species.setUrl(url);
        return species;
    }

    private SpellAffiliationLevelView level(String url, int level)
    {
        return new SpellAffiliationLevelView()
        {
            @Override
            public String getUrl()
            {
                return url;
            }

            @Override
            public Integer getLevel()
            {
                return level;
            }
        };
    }
}
