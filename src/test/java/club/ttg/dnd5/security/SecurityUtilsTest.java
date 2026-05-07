package club.ttg.dnd5.security;

import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.exception.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserReturnsDomainUserPrincipal() {
        User user = new User();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertSame(user, SecurityUtils.getUser());
    }

    @Test
    void getUserRejectsNonUserPrincipal() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("anonymousUser", null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ApiException exception = assertThrows(ApiException.class, SecurityUtils::getUser);

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }
}
