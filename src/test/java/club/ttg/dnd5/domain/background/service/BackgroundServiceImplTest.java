package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.background.repository.BackgroundRepository;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.mapper.BackgroundMapper;
import club.ttg.dnd5.domain.common.rest.dto.NameRequest;
import club.ttg.dnd5.domain.common.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.model.SourceType;
import club.ttg.dnd5.domain.source.service.SourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackgroundServiceImplTest {
    @Mock
    private BackgroundQueryDslSearchService backgroundQueryDslSearchService;
    @Mock
    private BackgroundRepository backgroundRepository;
    @Mock
    private FeatRepository featRepository;
    @Mock
    private SourceService sourceService;
    @Mock
    private EntityRevisionService revisionService;

    private BackgroundServiceImpl backgroundService;

    @BeforeEach
    void setUp() {
        BackgroundMapper backgroundMapper = Mappers.getMapper(BackgroundMapper.class);
        backgroundService = new BackgroundServiceImpl(
                backgroundQueryDslSearchService,
                backgroundRepository,
                featRepository,
                sourceService,
                backgroundMapper,
                revisionService
        );
    }

    @Test
    void addBackgroundAllowsNullFeat() {
        Source source = source();
        BackgroundRequest request = request(null, "clarifying text");

        when(backgroundRepository.existsById(request.getUrl())).thenReturn(false);
        when(sourceService.findReferenceByUrl(source.getUrl())).thenReturn(source);
        when(backgroundRepository.save(any(Background.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String url = backgroundService.addBackground(request);

        assertThat(url).isEqualTo(request.getUrl());
        verify(featRepository, never()).existsById(any());
        verify(featRepository, never()).getReferenceById(any());
    }

    @Test
    void updateBackgroundAllowsNullFeat() {
        Source source = source();
        Background existing = new Background();
        existing.setUrl("old-background");
        BackgroundRequest request = request(null, "clarifying text");

        when(backgroundRepository.findById(existing.getUrl())).thenReturn(Optional.of(existing));
        when(sourceService.findReferenceByUrl(source.getUrl())).thenReturn(source);
        when(backgroundRepository.save(any(Background.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String url = backgroundService.updateBackgrounds(existing.getUrl(), request);

        assertThat(url).isEqualTo(request.getUrl());
        verify(featRepository, never()).existsById(any());
        verify(featRepository, never()).getReferenceById(any());
    }

    @Test
    void previewShowsOnlySuffixWhenFeatIsNull() {
        Source source = source();
        BackgroundRequest request = request(null, "clarifying text");

        when(sourceService.findByUrl(source.getUrl())).thenReturn(source);

        BackgroundDetailResponse response = backgroundService.preview(request);

        assertThat(response.getFeat()).isEqualTo("\"clarifying text\"");
        verify(featRepository, never()).findById(any());
    }

    private BackgroundRequest request(String featUrl, String featSuffix) {
        BackgroundRequest request = new BackgroundRequest();
        request.setUrl("background");
        request.setName(name("background"));
        request.setDescription("background description");
        request.setSource(sourceRequest());
        request.setFeatUrl(featUrl);
        request.setFeatSuffix(featSuffix);
        request.setAbilityScores(Set.of());
        request.setSkillsProficiencies(Set.of());
        return request;
    }

    private NameRequest name(String value) {
        NameRequest name = new NameRequest();
        name.setName(value + " name");
        name.setEnglish(value + " english");
        name.setAlternative(List.of());
        return name;
    }

    private SourceRequest sourceRequest() {
        SourceRequest source = new SourceRequest();
        source.setUrl("source");
        source.setPage(1);
        return source;
    }

    private Source source() {
        Source source = new Source();
        source.setAcronym("SRC");
        source.setUrl("source");
        source.setName("Source");
        source.setEnglish("Source");
        source.setType(SourceType.OFFICIAL);
        return source;
    }
}
