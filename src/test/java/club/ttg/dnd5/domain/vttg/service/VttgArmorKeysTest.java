package club.ttg.dnd5.domain.vttg.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VttgArmorKeysTest {

    /** Все 13 базовых доспехов сайта — адресами, как они есть в разделе «Предметы». */
    @ParameterizedTest
    @CsvSource({
            "padded-armor-phb, padded",
            "leather-armor-phb, leather",
            "studded-leather-armor-phb, studded-leather",
            "hide-armor-phb, hide",
            "chain-shirt-phb, chain-shirt",
            "scale-mail-phb, scale-mail",
            "breastplate-phb, breastplate",
            "half-plate-armor-phb, half-plate",
            "ring-mail-phb, ring-mail",
            "chain-mail-phb, chain-mail",
            "splint-armor-phb, splint",
            "plate-armor-phb, plate",
            "shield-phb, shield"
    })
    void mapsSiteUrlToSheetKey(String url, String key) {
        assertEquals(key, VttgArmorKeys.ofUrl(url));
    }

    @Test
    void returnsNullForArmorOutsideSheetVocabulary() {
        assertNull(VttgArmorKeys.ofUrl("buckler-ghpg"));
        assertNull(VttgArmorKeys.ofUrl(null));
    }

    /** Отбрасывается только суффикс источника: «-armor-1-dmg» — уже не базовый доспех. */
    @Test
    void dropsOnlySourceSuffix() {
        assertNull(VttgArmorKeys.ofUrl("leather-armor-1-dmg"));
    }
}
