package club.ttg.dnd5.domain.spell.rest.controller;

import club.ttg.dnd5.domain.common.model.Visibility;
import club.ttg.dnd5.domain.spell.service.SpellFilterService;
import club.ttg.dnd5.domain.spell.service.SpellService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.pattern.PathPatternParser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Проверка роутинга {@link SpellController} после перехода на catch-all {@code /{*url}}:
 * homebrew-url со слэшами резолвятся, {@code ?raw} разводит форму и детальный ответ,
 * а литеральные подпути ({@code /search}, {@code /filters}) не перехватываются catch-all.
 * <p>
 * Standalone-setup с явным {@link PathPatternParser} — как в проде (Spring Boot использует
 * PathPattern по умолчанию), но без загрузки контекста приложения.
 */
class SpellControllerRoutingTest {

    private final SpellService spellService = mock(SpellService.class);
    private final SpellFilterService spellFilterService = mock(SpellFilterService.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SpellController(spellService, spellFilterService))
                .setPatternParser(new PathPatternParser())
                .build();
    }

    @Test
    void officialUrl_singleSegment_routesToDetailed() throws Exception {
        mockMvc.perform(get("/api/v2/spells/fireball-phb")).andExpect(status().isOk());
        verify(spellService).findDetailedByUrl("fireball-phb");
    }

    @Test
    void homebrewUrl_withSlashes_routesToDetailedWithFullKey() throws Exception {
        mockMvc.perform(get("/api/v2/spells/u/magistrus/fireball")).andExpect(status().isOk());
        // ключевая проверка: catch-all ловит все сегменты, ведущий слэш срезан
        verify(spellService).findDetailedByUrl("u/magistrus/fireball");
    }

    @Test
    void rawQueryFlag_routesToForm_notDetailed() throws Exception {
        mockMvc.perform(get("/api/v2/spells/u/magistrus/fireball").param("raw", "true"))
                .andExpect(status().isOk());
        verify(spellService).findFormByUrl("u/magistrus/fireball");
        verify(spellService, never()).findDetailedByUrl(any());
    }

    @Test
    void literalSearch_isNotSwallowedByCatchAll() throws Exception {
        mockMvc.perform(get("/api/v2/spells/search")).andExpect(status().isOk());
        verify(spellService).search(any());
        verify(spellService, never()).findDetailedByUrl(any());
    }

    @Test
    void literalFilters_isNotSwallowedByCatchAll() throws Exception {
        mockMvc.perform(get("/api/v2/spells/filters")).andExpect(status().isOk());
        verify(spellFilterService).getFilterMetadata(any());
        verify(spellService, never()).findDetailedByUrl(any());
    }

    @Test
    void homebrewCreate_routesToSaveHomebrew_withVisibilityParam() throws Exception {
        mockMvc.perform(post("/api/v2/spells/homebrew")
                        .param("visibility", "PUBLIC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());
        verify(spellService).saveHomebrew(any(), eq(Visibility.PUBLIC));
    }

    @Test
    void homebrewCreate_defaultsToPrivateVisibility() throws Exception {
        mockMvc.perform(post("/api/v2/spells/homebrew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());
        verify(spellService).saveHomebrew(any(), eq(Visibility.PRIVATE));
    }
}
