package club.ttg.dnd5.config;

import club.ttg.dnd5.config.properties.RateLimitProperties;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RateLimitFilterTest
{
    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

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

    @Test
    void anonymousClientsWithDifferentForwardedIpsDoNotShareBucket() throws ServletException, IOException
    {
        RateLimitFilter filter = singleTokenFilter();
        authenticate(anonymous());

        MockHttpServletRequest firstRequest = new MockHttpServletRequest("GET", "/api/v2/spells/search");
        firstRequest.addHeader("X-Forwarded-For", "203.0.113.1");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        MockHttpServletRequest secondRequest = new MockHttpServletRequest("GET", "/api/v2/spells/search");
        secondRequest.addHeader("X-Forwarded-For", "203.0.113.2");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();

        filter.doFilter(firstRequest, firstResponse, new MockFilterChain());
        filter.doFilter(secondRequest, secondResponse, new MockFilterChain());

        assertEquals(200, firstResponse.getStatus());
        assertEquals(200, secondResponse.getStatus());
    }

    @Test
    void anonymousClientsWithDifferentRemoteAddressesDoNotShareBucket() throws ServletException, IOException
    {
        RateLimitFilter filter = singleTokenFilter();
        authenticate(anonymous());

        MockHttpServletRequest firstRequest = new MockHttpServletRequest("GET", "/api/v2/spells/search");
        firstRequest.setRemoteAddr("198.51.100.1");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        MockHttpServletRequest secondRequest = new MockHttpServletRequest("GET", "/api/v2/spells/search");
        secondRequest.setRemoteAddr("198.51.100.2");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();

        filter.doFilter(firstRequest, firstResponse, new MockFilterChain());
        filter.doFilter(secondRequest, secondResponse, new MockFilterChain());

        assertEquals(200, firstResponse.getStatus());
        assertEquals(200, secondResponse.getStatus());
    }

    @Test
    void authenticatedUserSharesBucketAcrossIps() throws ServletException, IOException
    {
        RateLimitFilter filter = singleTokenFilter();
        authenticate(UsernamePasswordAuthenticationToken.authenticated(
                "player", null, AuthorityUtils.createAuthorityList("USER")));

        MockHttpServletRequest firstRequest = new MockHttpServletRequest("GET", "/api/v2/spells/search");
        firstRequest.addHeader("X-Forwarded-For", "203.0.113.1");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        MockHttpServletRequest secondRequest = new MockHttpServletRequest("GET", "/api/v2/spells/search");
        secondRequest.addHeader("X-Forwarded-For", "203.0.113.2");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();

        filter.doFilter(firstRequest, firstResponse, new MockFilterChain());
        filter.doFilter(secondRequest, secondResponse, new MockFilterChain());

        assertEquals(200, firstResponse.getStatus());
        assertEquals(429, secondResponse.getStatus());
    }

    private static RateLimitFilter singleTokenFilter()
    {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setCapacity(1);

        return new RateLimitFilter(properties);
    }

    private static AnonymousAuthenticationToken anonymous()
    {
        return new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    }

    private static void authenticate(Authentication authentication)
    {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
