package club.ttg.dnd5.domain.character_class.rest.controller;

import club.ttg.dnd5.domain.character_class.rest.dto.MulticlassResponse;
import club.ttg.dnd5.domain.character_class.service.MulticlassService;
import club.ttg.dnd5.domain.common.rest.dto.MulticlassRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.util.LinkedMultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MulticlassControllerTest {
    private final MulticlassService multiclassService = mock(MulticlassService.class);
    private final MulticlassController controller = new MulticlassController(multiclassService);

    @Test
    void getClassByQueryConvertsIndexedParamsToOrderedLevels() {
        var query = new LinkedMultiValueMap<String, String>();
        query.add("class1", "fighter-phb");
        query.add("level1", "3");
        query.add("subclass1", "battle-master-phb");
        query.add("class2", "bard-phb");
        query.add("level2", "3");
        query.add("subclass2", "bard-college-of-valor-phb");
        query.add("class3", "fighter-phb");
        query.add("level3", "6");
        query.add("subclass3", "battle-master-phb");

        when(multiclassService.getMulticlass(org.mockito.ArgumentMatchers.any(MulticlassRequest.class)))
                .thenReturn(new MulticlassResponse());

        controller.getClassByQuery(query);

        ArgumentCaptor<MulticlassRequest> captor = ArgumentCaptor.forClass(MulticlassRequest.class);
        verify(multiclassService).getMulticlass(captor.capture());

        var levels = captor.getValue().getLevels();
        assertEquals(3, levels.size());
        assertEquals("fighter-phb", levels.getFirst().getUrl());
        assertEquals(3, levels.get(0).getLevel());
        assertEquals("battle-master-phb", levels.get(0).getSubclass());
        assertEquals("bard-phb", levels.get(1).getUrl());
        assertEquals(3, levels.get(1).getLevel());
        assertEquals("bard-college-of-valor-phb", levels.get(1).getSubclass());
        assertEquals("fighter-phb", levels.get(2).getUrl());
        assertEquals(6, levels.get(2).getLevel());
        assertEquals("battle-master-phb", levels.get(2).getSubclass());
    }
}
