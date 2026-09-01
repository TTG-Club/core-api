package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.character_class.service.ClassService;
import club.ttg.dnd5.domain.feat.service.FeatService;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.service.SpeciesService;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.create.CreateAffiliationRequest;
import club.ttg.dnd5.domain.spell.rest.dto.create.SpellRequest;
import club.ttg.dnd5.domain.spell.rest.mapper.SpellMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

/**
 * Связь заклинания с видами живёт в таблице, общей с врождёнными заклинаниями вида,
 * и несёт чужую колонку {@code required_level}. Подменять коллекцию нельзя: Hibernate
 * переписал бы все строки и сбросил уровни. Проверяется, что сохранение правит
 * коллекцию на месте и уходит в базу только разницей.
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
    void updateChangesSpeciesCollectionInPlace()
    {
        Spell existing = spell();
        Set<Species> linked = new LinkedHashSet<>(Set.of(species("flamekin-lfl"), species("harpy-feq")));
        existing.setSpeciesAffiliation(linked);

        when(spellRepository.findForUpdateByUrl("fire-bolt-phb")).thenReturn(Optional.of(existing));
        when(speciesService.findAllById(Set.of("flamekin-lfl", "rimekin-lfl")))
                .thenReturn(Set.of(species("flamekin-lfl"), species("rimekin-lfl")));

        spellService.update("fire-bolt-phb", request(Set.of("flamekin-lfl", "rimekin-lfl")));

        // Та же коллекция, а не новая: иначе Hibernate переписал бы все строки связи.
        assertSame(linked, existing.getSpeciesAffiliation());
        assertEquals(Set.of("flamekin-lfl", "rimekin-lfl"), urls(existing.getSpeciesAffiliation()));
    }

    @Test
    void updateWithoutChangesLeavesSpeciesCollectionAsItWas()
    {
        Spell existing = spell();
        Set<Species> linked = new LinkedHashSet<>(Set.of(species("flamekin-lfl")));
        existing.setSpeciesAffiliation(linked);

        when(spellRepository.findForUpdateByUrl("fire-bolt-phb")).thenReturn(Optional.of(existing));
        when(speciesService.findAllById(Set.of("flamekin-lfl")))
                .thenReturn(Set.of(species("flamekin-lfl")));

        spellService.update("fire-bolt-phb", request(Set.of("flamekin-lfl")));

        assertSame(linked, existing.getSpeciesAffiliation());
        assertEquals(Set.of("flamekin-lfl"), urls(existing.getSpeciesAffiliation()));
    }

    @Test
    void updateFillsSpeciesCollectionWhenSpellHadNone()
    {
        Spell existing = spell();

        when(spellRepository.findForUpdateByUrl("fire-bolt-phb")).thenReturn(Optional.of(existing));
        when(speciesService.findAllById(Set.of("flamekin-lfl")))
                .thenReturn(Set.of(species("flamekin-lfl")));

        spellService.update("fire-bolt-phb", request(Set.of("flamekin-lfl")));

        assertEquals(Set.of("flamekin-lfl"), urls(existing.getSpeciesAffiliation()));
    }

    private Spell spell()
    {
        Spell spell = new Spell();
        spell.setUrl("fire-bolt-phb");
        spell.setLevel(0L);
        return spell;
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

    private Set<String> urls(Set<Species> species)
    {
        return species.stream().map(Species::getUrl).collect(Collectors.toSet());
    }
}
