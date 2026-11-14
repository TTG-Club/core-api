package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.source.model.Source;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * После смены адреса записи на сайте выгрузка VTTG отдаёт первоначальный адрес: на него
 * у пользователей VTTG завязаны имена файлов записей и ссылки листов персонажей.
 */
class VttgUrlAliasesTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {
        VttgUrlAliases.reset();
    }

    @Test
    void withoutAliasesNothingChanges() throws Exception {
        JsonNode payload = objectMapper.readTree("{\"id\":\"fireball-ph\"}");

        assertFalse(VttgUrlAliases.active());
        assertSame(payload, VttgUrlAliases.apply(payload));
        assertEquals("fireball-ph", VttgUrlAliases.original("fireball-ph"));
    }

    /** id, srcUrl, ссылки между записями и ссылки на сайт в тексте — с первоначальным адресом. */
    @Test
    void payloadGetsOriginalUrls() throws Exception {
        VttgUrlAliases.install(() -> Map.of("fireball-ph", "fireball-phb", "light-ph", "light-phb"));
        JsonNode payload = objectMapper.readTree("""
                {"id":"fireball-ph","srcUrl":"fireball-ph",
                 "description":"См. [свет](https://ttg.club/spells/light-ph) и greater-fireball-ph",
                 "grantedSpells":[{"url":"light-ph"}],
                 "byUrl":{"light-ph":true}}
                """);

        JsonNode result = VttgUrlAliases.apply(payload);

        assertEquals(objectMapper.readTree("""
                {"id":"fireball-phb","srcUrl":"fireball-phb",
                 "description":"См. [свет](https://ttg.club/spells/light-phb) и greater-fireball-ph",
                 "grantedSpells":[{"url":"light-phb"}],
                 "byUrl":{"light-phb":true}}
                """), result);
        assertEquals("light-phb", VttgUrlAliases.original("light-ph"));
    }

    /** id предмета с суффиксом источника считается от первоначального адреса, а не от нового. */
    @Test
    void itemIdStaysTheSame() {
        VttgItemMapper mapper = new VttgItemMapper(new VttgMarkupConverter(objectMapper));
        Item item = item("dagger-phb");
        Object before = mapper.toVttg(item).get("id");

        item.setUrl("dagger-ph");
        VttgUrlAliases.install(() -> Map.of("dagger-ph", "dagger-phb"));

        assertEquals("dagger-phb", before);
        assertEquals(before, mapper.toVttg(item).get("id"));
    }

    /** Ключ оружия ищется по первоначальному адресу — владение у персонажа не теряется. */
    @Test
    void weaponKeyUsesOriginalUrl() {
        String before = VttgWeaponKeys.ofUrl("dagger-phb");
        VttgUrlAliases.install(() -> Map.of("dagger-ph", "dagger-phb"));

        assertEquals(before, VttgWeaponKeys.ofUrl("dagger-ph"));
    }

    private static Item item(String url) {
        Item item = new Item();
        item.setUrl(url);
        item.setName("Кинжал");
        item.setEnglish("Dagger");
        item.setDescription("");
        Source source = new Source();
        source.setAcronym("PH");
        source.setVttgKey("phb");
        item.setSource(source);
        item.setSrdVersion("5.1");
        return item;
    }
}
