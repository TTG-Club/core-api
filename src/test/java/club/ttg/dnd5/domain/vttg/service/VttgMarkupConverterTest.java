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

    /** Ссылки на прочие разделы сайта — не только глоссарий и заклинания. */
    @Test
    void replacesOtherSectionMarkupWithSiteLink() {
        assertEquals(
                "[кинжал](https://ttg.club/items/dagger-phb)",
                converter.toText("{@item кинжал|url:dagger-phb}")
        );
        assertEquals(
                "[Бдительный](https://ttg.club/feats/alert-phb)",
                converter.toText("{@feat Бдительный|url:alert-phb}")
        );
        assertEquals(
                "[Гоблин](https://ttg.club/bestiary/goblin-mm)",
                converter.toText("{@creature Гоблин|url:goblin-mm}")
        );
        assertEquals(
                "[Сумка хранения](https://ttg.club/magic-items/bag-of-holding-dmg)",
                converter.toText("{@magicItem Сумка хранения|url:bag-of-holding-dmg}")
        );
    }

    /** Стартовое снаряжение предыстории целиком состоит из ссылок на предметы. */
    @Test
    void replacesEveryItemMarkupInEquipmentLine() {
        assertEquals(
                "2 [кинжала](https://ttg.club/items/dagger-phb), "
                        + "[воровские инструменты](https://ttg.club/items/thieves-tools-phb), "
                        + "[лом](https://ttg.club/items/crowbar-phb), "
                        + "2 [сумки](https://ttg.club/items/pouch-phb), "
                        + "[одежда путешественника](https://ttg.club/items/clothes-traveler-s-phb) и 16 зм",
                converter.toText("2 {@item кинжала|url:dagger-phb}, "
                        + "{@item воровские инструменты|url:thieves-tools-phb}, "
                        + "{@item лом|url:crowbar-phb}, "
                        + "2 {@item сумки|url:pouch-phb}, "
                        + "{@item одежда путешественника|url:clothes-traveler-s-phb} и 16 зм")
        );
    }

    /** Пробелы вокруг «|» и после «url:» встречаются в контенте и не должны ломать разбор. */
    @Test
    void toleratesSpacesAroundLinkAttribute() {
        assertEquals(
                "[лом](https://ttg.club/items/crowbar-phb)",
                converter.toText("{@item лом | url: crowbar-phb}")
        );
    }

    /** Незнакомый маркер разворачивается в метку — фигурные скобки в текст не уезжают. */
    @Test
    void unwrapsUnknownMarkerToItsLabel() {
        assertEquals("Урон", converter.toText("{@th Урон}"));
        assertEquals("Некий вид", converter.toText("{@species Некий вид|url:some-species}"));
    }

    /**
     * Оформление доезжает до VTTG в разметке его рендерера: markdown с GFM для
     * зачёркнутого и кода, инлайновый HTML для того, чего в markdown нет.
     */
    @Test
    void expandsFormattingMarkupForVttg() {
        assertEquals("**жирный**", converter.toText("{@b жирный}"));
        assertEquals("*курсив*", converter.toText("{@i курсив}"));
        assertEquals("*курсив*", converter.toText("{@em курсив}"));
        assertEquals("~~зачёркнутый~~", converter.toText("{@s зачёркнутый}"));
        assertEquals("~~зачёркнутый~~", converter.toText("{@strikethrough зачёркнутый}"));
        assertEquals("`код`", converter.toText("{@code код}"));
        assertEquals("<u>подчёркнутый</u>", converter.toText("{@u подчёркнутый}"));
        assertEquals("<sup>2</sup>", converter.toText("{@sup 2}"));
        assertEquals("<sub>n</sub>", converter.toText("{@sub n}"));
        assertEquals("<mark>важное</mark>", converter.toText("{@highlight важное}"));
        // Заголовок внутри абзаца отдельным уровнем не выразить — остаётся жирным.
        assertEquals("**Заголовок**", converter.toText("{@h Заголовок}"));
    }

    /** Вложенное оформление раскрывается целиком, а не рвётся по первой закрывающей скобке. */
    @Test
    void expandsNestedFormattingMarkup() {
        assertEquals(
                "**важно: <u>прочти</u>**",
                converter.toText("{@b важно: {@u прочти}}")
        );
        assertEquals(
                "*см. [кинжал](https://ttg.club/items/dagger-phb)*",
                converter.toText("{@i см. {@item кинжал|url:dagger-phb}}")
        );
    }

    /** У тега оформления тело — проза: вертикальная черта в ней не отрезает остаток. */
    @Test
    void keepsPipeInsideFormattingBody() {
        assertEquals("**2|3**", converter.toText("{@b 2|3}"));
    }

    /** Узловой диалект оформляется той же таблицей, что и литеральные маркеры. */
    @Test
    void expandsFormattingNodesForVttg() {
        String markup = """
                {
                  "type": "doc",
                  "content": [
                    {
                      "type": "paragraph",
                      "content": [
                        {"type": "underline", "content": [{"type": "text", "text": "важно"}]},
                        {"type": "text", "text": " и "},
                        {"type": "strike", "content": [{"type": "text", "text": "отменено"}]}
                      ]
                    }
                  ]
                }
                """;

        assertEquals("<u>важно</u> и ~~отменено~~", converter.toText(markup));
    }

    /** Тот же узловой диалект в промежуточном режиме — маркеры разметки не подставляются. */
    @Test
    void keepsFormattingNodesPlainForIntermediateTarget() {
        String markup = """
                {
                  "type": "doc",
                  "content": [
                    {
                      "type": "paragraph",
                      "content": [
                        {"type": "underline", "content": [{"type": "text", "text": "важно"}]}
                      ]
                    }
                  ]
                }
                """;

        assertEquals("важно", converter.toTextKeepingMarkers(markup));
    }

    /** Режим с сохранением бросков не должен быть съеден страховкой от утечки маркеров. */
    @Test
    void keepsRollMarkupInRollPreservingMode() {
        assertEquals(
                "Наносит {@roll 2к6} урона [кислотой](https://ttg.club/glossary/acid-phb)",
                converter.toTextKeepingRolls("Наносит {@roll 2к6} урона {@glossary кислотой|url:acid-phb}")
        );
    }

    /**
     * Промежуточный режим отдаёт теги оформления нетронутыми — их разбирают
     * форматтеры статей под свою целевую разметку.
     */
    @Test
    void keepsUnknownMarkersInMarkerPreservingMode() {
        assertEquals(
                "**Важно**: {@u подчёркнутый} и {@spoiler скрытый}",
                converter.toTextKeepingMarkers("{@b Важно}: {@u подчёркнутый} и {@spoiler скрытый}")
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
