package club.ttg.dnd5.domain.character_class.rest.controller;

import club.ttg.dnd5.domain.character_class.service.ClassFilterService;
import club.ttg.dnd5.domain.character_class.service.ClassService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.pattern.PathPatternParser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Роутинг {@link ClassController} после перехода на catch-all {@code /{*url}} — с проверкой того,
 * что вложенный {@code /{parentUrl}/subclasses} и литералы ({@code /subclasses}, {@code /search},
 * {@code /filters}, {@code /ability-improvement}) НЕ перехватываются catch-all для официальных url.
 */
class ClassControllerRoutingTest {

    private final ClassService classService = mock(ClassService.class);
    private final ClassFilterService classFilterService = mock(ClassFilterService.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ClassController(classService, classFilterService))
                .setPatternParser(new PathPatternParser())
                .build();
    }

    @Test
    void officialUrl_routesToDetail() throws Exception {
        mockMvc.perform(get("/api/v2/classes/wizard")).andExpect(status().isOk());
        verify(classService).findDetailedByUrl("wizard");
    }

    @Test
    void homebrewUrl_withSlashes_routesToDetail() throws Exception {
        mockMvc.perform(get("/api/v2/classes/u/magistrus/warlock")).andExpect(status().isOk());
        verify(classService).findDetailedByUrl("u/magistrus/warlock");
    }

    @Test
    void rawQueryFlag_routesToForm() throws Exception {
        mockMvc.perform(get("/api/v2/classes/wizard").param("raw", "true")).andExpect(status().isOk());
        verify(classService).findFormByUrl("wizard");
        verify(classService, never()).findDetailedByUrl(any());
    }

    @Test
    void nestedSubclasses_isNotSwallowedByCatchAll() throws Exception {
        mockMvc.perform(get("/api/v2/classes/wizard/subclasses")).andExpect(status().isOk());
        verify(classService).getSubclasses("wizard");
        verify(classService, never()).findDetailedByUrl(any());
    }

    @Test
    void literalSubclasses_routesToNoArgHandler() throws Exception {
        mockMvc.perform(get("/api/v2/classes/subclasses")).andExpect(status().isOk());
        verify(classService).getSubclasses();
        verify(classService, never()).findDetailedByUrl(any());
    }

    @Test
    void literalSearchFiltersAbility_areNotSwallowed() throws Exception {
        mockMvc.perform(get("/api/v2/classes/search")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v2/classes/filters")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v2/classes/ability-improvement")).andExpect(status().isOk());
        verify(classService).search(any());
        verify(classFilterService).getFilterMetadata(any());
        verify(classService).getAbilityImprovements();
        verify(classService, never()).findDetailedByUrl(any());
    }
}
