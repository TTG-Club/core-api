package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.repository.ClassRepository;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassDetailedResponse;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassEquipmentItemDto;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassEquipmentOptionDto;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassRequest;
import club.ttg.dnd5.domain.character_class.rest.mapper.ClassMapper;
import club.ttg.dnd5.domain.common.repository.GalleryRepository;
import club.ttg.dnd5.domain.item.repository.ItemNameRef;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private final EntityRevisionService revisionService = mock(EntityRevisionService.class);
    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final ClassService service = new ClassService(
            classRepository,
            classMapper,
            classQueryDslSearchService,
            sourceService,
            galleryRepository,
            sourceSavedFilterService,
            revisionService,
            itemRepository
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
        // Снимок версии после переименования читает сущность по новому url.
        when(classRepository.findById("new-url")).thenReturn(Optional.of(mappedClass));
        when(classMapper.toRequest(mappedClass)).thenReturn(new ClassRequest());

        service.update("old-url", request);

        ArgumentCaptor<CharacterClass> captor = ArgumentCaptor.forClass(CharacterClass.class);
        verify(classRepository).save(captor.capture());
        verify(classRepository, never()).getReferenceById(any());
        assertNull(captor.getValue().getParent());
    }

    @Test
    void detailedResponseUsesActualItemNames() {
        CharacterClass characterClass = new CharacterClass();
        characterClass.setUrl("bard");

        var renamed = new ClassEquipmentItemDto("dagger", "Название на момент сохранения", 2, null);
        var missing = new ClassEquipmentItemDto("lost-item", "Снимок", 1, null);
        ClassDetailedResponse response = new ClassDetailedResponse();
        response.setStartingEquipment(List.of(
                new ClassEquipmentOptionDto("А", List.of(renamed, missing), 15, "зм")
        ));

        List<ItemNameRef> found = List.of(itemName("dagger", "Кинжал"));

        when(classRepository.findById("bard")).thenReturn(Optional.of(characterClass));
        when(classMapper.toDetailedResponse(characterClass)).thenReturn(response);
        when(itemRepository.findNamesByUrls(Set.of("dagger", "lost-item"))).thenReturn(found);

        var items = service.findDetailedByUrl("bard").getStartingEquipment().getFirst().getItems();

        assertEquals("Кинжал", items.getFirst().getName());
        // Предмета уже нет в справочнике — остаётся название из снимка.
        assertEquals("Снимок", items.get(1).getName());
    }

    private ItemNameRef itemName(String url, String name) {
        ItemNameRef ref = mock(ItemNameRef.class);
        when(ref.getUrl()).thenReturn(url);
        when(ref.getName()).thenReturn(name);
        return ref;
    }
}
