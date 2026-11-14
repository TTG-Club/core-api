package club.ttg.dnd5.domain.vttg.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VttgSourceKeysTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Книга подкласса попадает в словарь наравне с книгой самой записи. */
    @Test
    void collectsNestedSubclassSources() throws Exception {
        String payload = """
                {"sourceKey":"phb","subclasses":[
                  {"key":"arcane-archer-uaasu","sourceKey":"uaasu"},
                  {"key":"arcane-archer-uaau","sourceKey":"uaau"},
                  {"key":"champion"}
                ]}
                """;
        Set<String> keys = new HashSet<>();

        VttgSourceKeys.collectSourceKeys(objectMapper.readTree(payload), keys);

        assertEquals(Set.of("phb", "uaasu", "uaau"), keys);
    }

    /** После смены акронима ключ источника в выгрузке остаётся прежним. */
    @Test
    void renamedSourceKeepsFrozenKey() {
        club.ttg.dnd5.domain.source.model.Source source = new club.ttg.dnd5.domain.source.model.Source();
        source.setAcronym("XGTE");
        assertEquals("xgte", VttgSourceKeys.of(source));

        source.setVttgKey("xge");

        assertEquals("xge", VttgSourceKeys.of(source));
        assertEquals("phb", VttgSourceKeys.ofAcronym("PHB24"));
        assertEquals(VttgSourceKeys.FALLBACK, VttgSourceKeys.of(null));
    }

    /** Разделители черт приходят картой — у них только собственный ключ. */
    @Test
    void collectsSourceKeyOfMapPayload() {
        Set<String> keys = new HashSet<>();

        VttgSourceKeys.collectSourceKeys(Map.of("sourceKey", "ghpg"), keys);
        VttgSourceKeys.collectSourceKeys(Map.of("name", "separator"), keys);

        assertEquals(Set.of("ghpg"), keys);
    }
}
