package club.ttg.dnd5.dto.base.filters;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentVisibilityPredicateTest {

    @Test
    void official_isOwnerIdIsNull() {
        assertEquals("ownerId is null", ContentVisibilityPredicate.official().toString());
    }

    @Test
    void publicHomebrew_requiresOwnerAndPublicVisibility() {
        assertEquals("ownerId is not null && visibility = PUBLIC",
                ContentVisibilityPredicate.publicHomebrew().toString());
    }

    @Test
    void listableFor_anonymous_isOfficialOrPublicHomebrew() {
        String sql = ContentVisibilityPredicate.listableFor(null).toString();
        assertEquals("ownerId is null || ownerId is not null && visibility = PUBLIC", sql);
    }

    @Test
    void listableFor_authenticated_alsoIncludesOwnContent() {
        UUID user = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String sql = ContentVisibilityPredicate.listableFor(user).toString();
        assertTrue(sql.contains("ownerId is null"), sql);
        assertTrue(sql.contains("visibility = PUBLIC"), sql);
        assertTrue(sql.contains("ownerId = " + user), sql);
    }

    @Test
    void readableByLinkFor_anonymous_excludesPrivate() {
        String sql = ContentVisibilityPredicate.readableByLinkFor(null).toString();
        assertEquals("ownerId is null || visibility != PRIVATE", sql);
    }

    @Test
    void readableByLinkFor_authenticated_includesOwnPrivate() {
        UUID user = UUID.fromString("00000000-0000-0000-0000-000000000002");
        String sql = ContentVisibilityPredicate.readableByLinkFor(user).toString();
        assertTrue(sql.contains("visibility != PRIVATE"), sql);
        assertTrue(sql.contains("ownerId = " + user), sql);
    }
}
