package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.repository.ClassRepository;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassRequest;
import club.ttg.dnd5.domain.character_class.rest.mapper.ClassMapper;
import club.ttg.dnd5.domain.common.repository.GalleryRepository;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClassServiceTest {
    private final ClassRepository classRepository = mock(ClassRepository.class);
    private final ClassMapper classMapper = mock(ClassMapper.class);
    private final ClassQueryDslSearchService classQueryDslSearchService = mock(ClassQueryDslSearchService.class);
    private final SourceService sourceService = mock(SourceService.class);
    private final GalleryRepository galleryRepository = mock(GalleryRepository.class);
    private final SourceSavedFilterService sourceSavedFilterService = mock(SourceSavedFilterService.class);
    private final ClassService service = new ClassService(
            classRepository,
            classMapper,
            classQueryDslSearchService,
            sourceService,
            galleryRepository,
            sourceSavedFilterService
    );

    @Test
    void updateRenamedRootClassDoesNotLookupNullParent() {
        CharacterClass existingClass = new CharacterClass();
        existingClass.setUrl("old-url");

        CharacterClass mappedClass = new CharacterClass();
        mappedClass.setUrl("new-url");

        ClassRequest request = new ClassRequest();
        request.setUrl("new-url");

        when(classRepository.findById("old-url")).thenReturn(Optional.of(existingClass));
        when(classMapper.toEntity(request, null)).thenReturn(mappedClass);

        service.update("old-url", request);

        ArgumentCaptor<CharacterClass> captor = ArgumentCaptor.forClass(CharacterClass.class);
        verify(classRepository).save(captor.capture());
        verify(classRepository, never()).getReferenceById(any());
        assertNull(captor.getValue().getParent());
    }
}
