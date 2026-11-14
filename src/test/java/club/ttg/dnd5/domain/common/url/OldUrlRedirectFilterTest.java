package club.ttg.dnd5.domain.common.url;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OldUrlRedirectFilterTest {
    private final EntityUrlHistoryRegistry history = mock(EntityUrlHistoryRegistry.class);
    private final OldUrlRedirectFilter filter = new OldUrlRedirectFilter(history);

    /** Старый адрес → запрос идёт к записи по новому, новый адрес — в заголовке. */
    @Test
    void oldUrlIsServedByCurrentRecord() throws Exception {
        when(history.currentUrl(anyString(), anyString())).thenReturn(Optional.empty());
        when(history.currentUrl("spells", "fireball-phb")).thenReturn(Optional.of("fireball-ph"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v2/spells/fireball-phb/raw");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        ArgumentCaptor<HttpServletRequest> passed = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(chain).doFilter(passed.capture(), org.mockito.ArgumentMatchers.eq(response));
        assertEquals("/api/v2/spells/fireball-ph/raw", passed.getValue().getRequestURI());
        assertEquals("/api/v2/spells/fireball-ph/raw", passed.getValue().getServletPath());
        assertEquals("fireball-ph", response.getHeader(OldUrlRedirectFilter.HEADER));
    }

    /** Живой адрес и чужие пути не трогаются. */
    @Test
    void otherRequestsPassUnchanged() throws Exception {
        when(history.currentUrl(anyString(), anyString())).thenReturn(Optional.empty());
        for (String uri : new String[]{"/api/v2/spells/fireball-ph", "/api/v2/spells/search",
                "/api/v2/articles/fireball-phb", "/api/v2/spells", "/actuator/health"}) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            assertNull(response.getHeader(OldUrlRedirectFilter.HEADER));
        }
    }

    /** Синоним пути раздела предметов тоже понимается; раздел — по сегменту пути. */
    @Test
    void itemAliasPathUsesItemsSection() {
        when(history.currentUrl("items", "dagger-phb")).thenReturn(Optional.of("dagger-ph"));

        OldUrlRedirectFilter.Rewrite rewrite = filter.rewrite("/api/v2/item/dagger-phb", "").orElseThrow();

        assertEquals("/api/v2/item/dagger-ph", rewrite.requestUri());
        assertSame(Optional.empty(), filter.rewrite("/api/v2/magic-items/dagger-phb", ""));
    }
}
