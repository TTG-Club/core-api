package club.ttg.dnd5.domain.species.service;

import club.ttg.dnd5.domain.common.service.GrantedSpellResolver;

import club.ttg.dnd5.domain.common.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.repository.SpeciesRepository;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesInnateSpellRequest;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesRequest;
import club.ttg.dnd5.domain.species.rest.mapper.SpeciesFeatureMapper;
import club.ttg.dnd5.domain.species.rest.mapper.SpeciesMapper;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.mapper.SpellMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpeciesServiceTest
{
    private final SpeciesRepository speciesRepository = mock(SpeciesRepository.class);
    private final SourceService sourceService = mock(SourceService.class);
    private final SpeciesQueryDslSearchService searchService = mock(SpeciesQueryDslSearchService.class);
    private final SpeciesMapper speciesMapper = mock(SpeciesMapper.class);
    private final SpeciesFeatureMapper featureMapper = mock(SpeciesFeatureMapper.class);
    private final SourceSavedFilterService sourceSavedFilterService = mock(SourceSavedFilterService.class);
    private final EntityRevisionService revisionService = mock(EntityRevisionService.class);
    private final SpellRepository spellRepository = mock(SpellRepository.class);
    private final SpellMapper spellMapper = mock(SpellMapper.class);
    private final GrantedSpellResolver grantedSpellResolver = mock(GrantedSpellResolver.class);
    private final SpeciesService service = new SpeciesService(
            speciesRepository,
            sourceService,
            searchService,
            speciesMapper,
            featureMapper,
            sourceSavedFilterService,
            revisionService,
            spellRepository,
            spellMapper,
            grantedSpellResolver
    );

    @Test
    void updateLineageStoresInnateSpellInExistingLineageAffiliation()
    {
        Species parent = new Species();
        Species lineage = new Species();
        lineage.setUrl("forest-gnome");
        lineage.setParent(parent);

        SpeciesInnateSpellRequest innateSpell = new SpeciesInnateSpellRequest();
        innateSpell.setSpell("minor-illusion");
        innateSpell.setRequiredLevel(3);

        SourceRequest source = new SourceRequest();
        source.setUrl("phb");

        SpeciesRequest request = new SpeciesRequest();
        request.setUrl(lineage.getUrl());
        request.setParent("gnome");
        request.setSource(source);
        request.setInnateSpells(List.of(innateSpell));

        when(speciesRepository.findById(lineage.getUrl())).thenReturn(Optional.of(lineage));
        when(speciesRepository.findById("gnome")).thenReturn(Optional.of(parent));
        when(speciesRepository.save(lineage)).thenReturn(lineage);
        when(speciesRepository.findInnateSpells(lineage.getUrl())).thenReturn(List.of());
        when(speciesMapper.toRequest(lineage)).thenReturn(new SpeciesRequest());
        when(spellRepository.countByUrlIn(Set.of("minor-illusion"))).thenReturn(1L);

        service.update(lineage.getUrl(), request);

        verify(speciesRepository).deleteSpeciesInnateSpells(lineage.getUrl());
        verify(speciesRepository).deleteLineageInnateSpells(lineage.getUrl());
        verify(speciesRepository).addLineageInnateSpell(
                lineage.getUrl(),
                "minor-illusion",
                3
        );
    }
}
