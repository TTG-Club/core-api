package club.ttg.dnd5.domain.species.rest.controller;

import club.ttg.dnd5.domain.species.service.SpeciesFilterService;
import club.ttg.dnd5.domain.species.service.SpeciesService;
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
 * Роутинг {@link SpeciesController} после перехода на catch-all {@code /{*url}} — с проверкой того,
 * что вложенные односегментные ресурсы ({@code /{url}/lineages}, {@code /{url}/lineages/search})
 * и литералы ({@code /lineages}, {@code /search}, {@code /filters}) НЕ перехватываются catch-all
 * для официальных (односегментных) url.
 */
class SpeciesControllerRoutingTest {

    private final SpeciesService speciesService = mock(SpeciesService.class);
    private final SpeciesFilterService speciesFilterService = mock(SpeciesFilterService.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SpeciesController(speciesService, speciesFilterService))
                .setPatternParser(new PathPatternParser())
                .build();
    }

    @Test
    void officialUrl_routesToDetail() throws Exception {
        mockMvc.perform(get("/api/v2/species/human")).andExpect(status().isOk());
        verify(speciesService).findById("human");
    }

    @Test
    void homebrewUrl_withSlashes_routesToDetail() throws Exception {
        mockMvc.perform(get("/api/v2/species/u/magistrus/elf")).andExpect(status().isOk());
        verify(speciesService).findById("u/magistrus/elf");
    }

    @Test
    void rawQueryFlag_routesToForm() throws Exception {
        mockMvc.perform(get("/api/v2/species/human").param("raw", "true")).andExpect(status().isOk());
        verify(speciesService).findFormByUrl("human");
        verify(speciesService, never()).findById(any());
    }

    @Test
    void nestedLineages_isNotSwallowedByCatchAll() throws Exception {
        mockMvc.perform(get("/api/v2/species/human/lineages")).andExpect(status().isOk());
        verify(speciesService).getLineages("human");
        verify(speciesService, never()).findById(any());
    }

    @Test
    void nestedLineagesSearch_isNotSwallowedByCatchAll() throws Exception {
        mockMvc.perform(get("/api/v2/species/elf/lineages/search")).andExpect(status().isOk());
        verify(speciesService).getAllLineages("elf");
        verify(speciesService, never()).findById(any());
    }

    @Test
    void literalLineages_routesToNoArgHandler() throws Exception {
        mockMvc.perform(get("/api/v2/species/lineages")).andExpect(status().isOk());
        verify(speciesService).getLineages();
        verify(speciesService, never()).findById(any());
    }

    @Test
    void literalSearchAndFilters_areNotSwallowed() throws Exception {
        mockMvc.perform(get("/api/v2/species/search")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v2/species/filters")).andExpect(status().isOk());
        verify(speciesService).search(any());
        verify(speciesFilterService).getFilterMetadata(any());
        verify(speciesService, never()).findById(any());
    }
}
