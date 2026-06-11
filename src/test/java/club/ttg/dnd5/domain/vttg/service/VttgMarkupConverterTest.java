package club.ttg.dnd5.domain.vttg.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VttgMarkupConverterTest {
    private final VttgMarkupConverter converter = new VttgMarkupConverter(new ObjectMapper());

    @Test
    void preservesFullDescriptionFromNestedMarkup() {
        String markup = """
                {
                  "type": "doc",
                  "content": [
                    {
                      "type": "paragraph",
                      "content": [
                        {"type": "text", "text": "Цель получает "},
                        {"type": "roll", "text": "1к8"},
                        {"type": "text", "text": " урона огнём."}
                      ]
                    },
                    {
                      "type": "paragraph",
                      "content": [
                        {"type": "text", "text": "После этого цель горит."}
                      ]
                    }
                  ]
                }
                """;

        assertEquals(
                "Цель получает 1к8 урона огнём.\n\nПосле этого цель горит.",
                converter.toText(markup)
        );
    }

    @Test
    void preservesListsAndHardBreaks() {
        String markup = """
                {
                  "type": "bulletList",
                  "content": [
                    {
                      "type": "listItem",
                      "content": [{
                        "type": "paragraph",
                        "content": [
                          {"type": "text", "text": "Первый эффект"},
                          {"type": "hardBreak"},
                          {"type": "text", "text": "продолжение"}
                        ]
                      }]
                    },
                    {
                      "type": "listItem",
                      "content": [{
                        "type": "paragraph",
                        "content": [{"type": "text", "text": "Второй эффект"}]
                      }]
                    }
                  ]
                }
                """;

        assertEquals(
                "- Первый эффект\nпродолжение\n\n- Второй эффект",
                converter.toText(markup)
        );
    }

    @Test
    void replacesGlossaryMarkupWithSiteLink() {
        assertEquals(
                "Существо находится в [сфере](https://ttg.club/glossary/sphere-phb).",
                converter.toText("Существо находится в {@glossary сфере|url:sphere-phb}.")
        );
    }

    @Test
    void replacesSpellMarkupWithSiteLink() {
        assertEquals(
                "[Detect Magic [Detect Magic]](https://ttg.club/spells/detect-magic-phb)",
                converter.toText("{@spell Detect Magic [Detect Magic]|url:detect-magic-phb}")
        );
    }

    @Test
    void replacesInlineRollMarkupWithDisplayedFormula() {
        assertEquals("+1", converter.toText("{@roll +1|notation:1d20+1}"));
        assertEquals("2к6", converter.toText("{@roll 2к6}"));
    }

    @Test
    void convertsRealSpellDescriptionArray() {
        String markup = """
                ["{@i Вы бросаете кислотный шарик} в точку в пределах дальности, где он взрывается {@glossary сферой|url:sphere-phb} с радиусом 5 фт. Каждое {@glossary существо|url:creature-phb} в этой сфере должно преуспеть в {@glossary спасброске|url:saving-throw-phb} Ловкости или получить {@roll 1к6} урона кислотой."]
                """;

        assertEquals(
                "*Вы бросаете кислотный шарик* в точку в пределах дальности, где он взрывается "
                        + "[сферой](https://ttg.club/glossary/sphere-phb) с радиусом 5 фт. Каждое "
                        + "[существо](https://ttg.club/glossary/creature-phb) в этой сфере должно преуспеть в "
                        + "[спасброске](https://ttg.club/glossary/saving-throw-phb) Ловкости или получить "
                        + "1к6 урона кислотой.",
                converter.toText(markup)
        );
    }

    @Test
    void convertsDoubleEncodedDescriptionArray() throws Exception {
        String description = "[\"Текст с {@roll 1к6} урона.\"]";
        String doubleEncoded = new ObjectMapper().writeValueAsString(description);

        assertEquals("Текст с 1к6 урона.", converter.toText(doubleEncoded));
    }

    @Test
    void doesNotReplaceUnknownNonEmptyMarkupWithEmptyDescription() {
        String markup = "{\"unknown\":\"Содержимое\"}";

        assertEquals(markup, converter.toText(markup));
    }
}
