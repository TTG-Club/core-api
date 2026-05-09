package club.ttg.dnd5.config;

import club.ttg.dnd5.config.properties.RateLimitProperties;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RateLimitFilterTest
{
    @Test
    void heartbeatEndpointSkipsRateLimit() throws ServletException, IOException
    {
        RateLimitFilter filter = new RateLimitFilter(new RateLimitProperties());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v2/online/heartbeat");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertNull(response.getHeader("X-RateLimit-Remaining"));
    }

    @Test
    void otherEndpointsUseRateLimit() throws ServletException, IOException
    {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setCapacity(1);

        RateLimitFilter filter = new RateLimitFilter(properties);
        MockHttpServletRequest firstRequest = new MockHttpServletRequest("GET", "/api/v2/online/count");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        MockHttpServletRequest secondRequest = new MockHttpServletRequest("GET", "/api/v2/online/count");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();

        filter.doFilter(firstRequest, firstResponse, new MockFilterChain());
        filter.doFilter(secondRequest, secondResponse, new MockFilterChain());

        assertEquals(200, firstResponse.getStatus());
        assertEquals("0", firstResponse.getHeader("X-RateLimit-Remaining"));
        assertEquals(429, secondResponse.getStatus());
    }
}
