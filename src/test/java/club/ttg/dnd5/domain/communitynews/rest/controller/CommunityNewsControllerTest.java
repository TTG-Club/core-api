package club.ttg.dnd5.domain.communitynews.rest.controller;

import club.ttg.dnd5.domain.communitynews.model.CommunityNewsRoles;
import club.ttg.dnd5.domain.communitynews.rest.dto.CommunityNewsRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authorization.method.SecuredAuthorizationManager;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.util.SimpleMethodInvocation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Права ручек новостей сообщества: правят только редакторы, читают все. Проверяем реальным
 * {@link SecuredAuthorizationManager} — тем же, что решает в приложении (с мета-аннотацией).
 */
class CommunityNewsControllerTest {

    private static final Set<String> EDITOR_ONLY = Set.of(
            "existByUrl", "getRawByUrl", "create", "update", "preview", "delete", "searchUnpublished");

    private static final Set<String> PUBLIC = Set.of("findByUrl", "search");

    private final SecuredAuthorizationManager authorization = new SecuredAuthorizationManager();

    private static Map<String, Method> endpoints() {
        return Arrays.stream(CommunityNewsController.class.getDeclaredMethods())
                .filter(method -> java.lang.reflect.Modifier.isPublic(method.getModifiers()))
                .collect(Collectors.toMap(Method::getName, method -> method));
    }

    private boolean allowed(Method method, Authentication authentication) {
        return authorization.authorize(() -> authentication,
                new SimpleMethodInvocation(null, method)).isGranted();
    }

    @Test
    void everyEndpointIsClassified() {
        assertEquals(EDITOR_ONLY.size() + PUBLIC.size(), endpoints().size());
        assertTrue(endpoints().keySet().containsAll(EDITOR_ONLY));
        assertTrue(endpoints().keySet().containsAll(PUBLIC));
    }

    @Test
    void editorRolesComeFromOnePlace() {
        Secured secured = AnnotatedElementUtils.findMergedAnnotation(
                endpoints().get("create"), Secured.class);
        assertArrayEquals(CommunityNewsRoles.EDITORS, secured.value());
    }

    @Test
    void mutatingEndpointsAreForEditorsOnly() {
        Authentication admin = new TestingAuthenticationToken("a", "p", "ADMIN");
        Authentication moderator = new TestingAuthenticationToken("m", "p", "MODERATOR");
        Authentication user = new TestingAuthenticationToken("u", "p", "USER");
        Authentication anonymous = new AnonymousAuthenticationToken("key", "anon",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        for (String name : EDITOR_ONLY) {
            Method method = endpoints().get(name);
            assertTrue(allowed(method, admin), name + ": ADMIN");
            assertTrue(allowed(method, moderator), name + ": MODERATOR");
            assertFalse(allowed(method, user), name + ": USER");
            assertFalse(allowed(method, anonymous), name + ": аноним");
        }
    }

    @Test
    void publicEndpointsHaveNoRoleCheck() {
        for (String name : PUBLIC) {
            assertNull(AnnotatedElementUtils.findMergedAnnotation(endpoints().get(name), Secured.class), name);
        }
    }

    @Test
    void rawFormSerializesEmptyTextsAsEmptyStrings() throws Exception {
        CommunityNewsRequest form = new CommunityNewsRequest();
        form.setUrl("raw");
        form.setTitle("Новость");
        form.setAuthorName("");
        form.setPreview("");
        form.setContent(null);

        JsonNode json = new ObjectMapper().findAndRegisterModules().valueToTree(form);

        assertEquals("", json.get("preview").asText());
        assertTrue(json.get("content").isTextual());
        assertEquals("", json.get("content").asText());
        assertEquals("", json.get("authorName").asText());

        // Полная новость без текста: фронт ждёт разметку (строку), а не null.
        club.ttg.dnd5.domain.communitynews.rest.dto.CommunityNewsDetailedResponse detailed =
                new club.ttg.dnd5.domain.communitynews.rest.dto.CommunityNewsDetailedResponse();
        detailed.setPreview("[\"анонс\"]");
        JsonNode detailedJson = new ObjectMapper().findAndRegisterModules().valueToTree(detailed);
        assertTrue(detailedJson.get("content").isTextual());
        assertEquals("", detailedJson.get("content").asText());
        detailed.setContent("[\"текст\"]");
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        detailed.setContent("  \n ");
        // Пробелы нельзя писать «сырым» значением — получился бы невалидный JSON.
        assertEquals("", objectMapper.readTree(objectMapper.writeValueAsString(detailed)).get("content").asText());
        detailed.setContent("[\"текст\"]");
        assertTrue(objectMapper.readTree(objectMapper.writeValueAsString(detailed)).get("content").isArray());

        // Пустой content из формы принимается обратно как пустая строка, а не ломает разбор.
        CommunityNewsRequest back = new ObjectMapper().findAndRegisterModules()
                .readValue("{\"content\":\"\",\"preview\":\"[\\\"анонс\\\"]\"}", CommunityNewsRequest.class);
        assertEquals("", back.getContent());
    }
}
