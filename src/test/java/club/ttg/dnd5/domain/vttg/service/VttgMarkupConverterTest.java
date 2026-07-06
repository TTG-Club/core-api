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
                "- Первый эффект\n  продолжение\n- Второй эффект",
                converter.toText(markup)
        );
    }

    @Test
    void convertsOrderedListToMarkdown() {
        String markup = """
                {
                  "type": "orderedList",
                  "attrs": {"start": 3},
                  "content": [
                    {
                      "type": "listItem",
                      "content": [{
                        "type": "paragraph",
                        "content": [{"type": "text", "text": "First"}]
                      }]
                    },
                    {
                      "type": "listItem",
                      "content": [{
                        "type": "paragraph",
                        "content": [{"type": "text", "text": "Second"}]
                      }]
                    }
                  ]
                }
                """;

        assertEquals(
                "3. First\n4. Second",
                converter.toText(markup)
        );
    }

    @Test
    void convertsTableToMarkdown() {
        String markup = """
                {
                  "type": "table",
                  "content": [
                    {
                      "type": "tableRow",
                      "content": [
                        {
                          "type": "tableHeader",
                          "content": [{
                            "type": "paragraph",
                            "content": [{"type": "text", "text": "Name"}]
                          }]
                        },
                        {
                          "type": "tableHeader",
                          "content": [{
                            "type": "paragraph",
                            "content": [{"type": "text", "text": "Value"}]
                          }]
                        }
                      ]
                    },
                    {
                      "type": "tableRow",
                      "content": [
                        {
                          "type": "tableCell",
                          "content": [{
                            "type": "paragraph",
                            "content": [{"type": "text", "text": "A | B"}]
                          }]
                        },
                        {
                          "type": "tableCell",
                          "content": [{
                            "type": "paragraph",
                            "content": [
                              {"type": "text", "text": "Line 1"},
                              {"type": "hardBreak"},
                              {"type": "text", "text": "Line 2"}
                            ]
                          }]
                        }
                      ]
                    }
                  ]
                }
                """;

        assertEquals(
                "| Name | Value |\n| --- | --- |\n| A \\| B | Line 1<br>Line 2 |",
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
    void unwrapsLinkTokenToPlainTerm() {
        assertEquals(
                "Только рукопашное оружие.",
                converter.toText("Только {@link рукопашное оружие|url:/items?itemType=MELEE_WEAPON}.")
        );
        // Без url — тоже остаётся только термин.
        assertEquals("оружие", converter.toText("{@link оружие}"));
    }

    @Test
    void replacesBrTokenWithLineBreak() {
        assertEquals(
                "Первая строка\nВторая строка",
                converter.toText("Первая строка{@br}Вторая строка")
        );
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

    @Test
    void convertsFrontendDialectTableToMarkdown() {
        String markup = """
                {
                  "type": "table",
                  "caption": "Пример",
                  "colLabels": ["Компонент", "Тип"],
                  "colStyles": ["w-1/2", "w-1/2"],
                  "rows": [
                    [
                      {"content": [{"type": "bold", "content": [{"type": "text", "text": "Badge"}]}]},
                      "Inline"
                    ],
                    [
                      "Kbd",
                      {"content": [{"type": "badge", "attrs": {"color": "primary"}, "content": [{"type": "text", "text": "7"}]}]}
                    ]
                  ]
                }
                """;

        assertEquals(
                "| Компонент | Тип |\n| --- | --- |\n| **Badge** | Inline |\n| Kbd | 7 |",
                converter.toText(markup)
        );
    }

    @Test
    void convertsFrontendTableWithNodeArrayHeaders() {
        // Реальный формат редактора (toStoredMarkup): colLabels[i] — МАССИВ инлайн-
        // узлов, ячейки — {content}. Заголовок из нескольких фрагментов не должен
        // склеиваться через <br> (инлайн-склейка, а не блочная).
        String markup = """
                {
                  "type": "table",
                  "colLabels": [
                    [
                      {"type": "text", "text": "Урон ("},
                      {"type": "roll", "content": [{"type": "text", "text": "к6"}]},
                      {"type": "text", "text": ")"}
                    ],
                    [{"type": "text", "text": "Эффект"}]
                  ],
                  "rows": [
                    [
                      {"content": [{"type": "text", "text": "10"}]},
                      {"content": [{"type": "text", "text": "Ожог"}]}
                    ]
                  ]
                }
                """;

        assertEquals(
                "| Урон (к6) | Эффект |\n| --- | --- |\n| 10 | Ожог |",
                converter.toText(markup)
        );
    }

    @Test
    void convertsFrontendDialectOrderedListToMarkdown() {
        String markup = """
                {
                  "type": "list",
                  "attrs": {"type": "ordered"},
                  "content": [
                    [{"type": "text", "text": "Первый"}],
                    [{"type": "bold", "content": [{"type": "text", "text": "Второй"}]}]
                  ]
                }
                """;

        assertEquals("1. Первый\n2. **Второй**", converter.toText(markup));
    }

    @Test
    void convertsFrontendDialectQuoteWithInlineNodes() {
        String markup = """
                {
                  "type": "quote",
                  "attrs": {"color": "primary", "variant": "outline"},
                  "content": [
                    {"type": "bold", "content": [{"type": "text", "text": "Внимание"}]},
                    {"type": "text", "text": ": смотри "},
                    {"type": "spell", "attrs": {"url": "fireball-phb"}, "content": [{"type": "text", "text": "Огненный шар"}]},
                    {"type": "text", "text": "."}
                  ]
                }
                """;

        assertEquals(
                "**Внимание**: смотри [Огненный шар](https://ttg.club/spells/fireball-phb).",
                converter.toText(markup)
        );
    }
}
