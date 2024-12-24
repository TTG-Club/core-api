package club.ttg.dnd5.controller.tools.dictionary;

import club.ttg.dnd5.dto.select.DiceOptionDto;
import club.ttg.dnd5.dto.select.SelectOptionDto;
import club.ttg.dnd5.dto.select.SpellcasterOptionDto;
import club.ttg.dnd5.service.base.DictionariesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * Различные справочники
 */
@RequiredArgsConstructor
@Tag(name = "Справочники", description = "API для различных справочников")
@RequestMapping("/api/v2/dictionaries")
@RestController
public class DictionariesController {
    private final DictionariesService dictionariesService;

    @Operation(summary = "Дайсы")
    @GetMapping("/dices")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject("""
                                            [
                                              {
                                                "label": "к4",
                                                "value": "d4",
                                                "maxValue": 4
                                              },
                                              {
                                                "label": "к6",
                                                "value": "d6",
                                                "maxValue": 6
                                              },
                                              {
                                                "label": "к8",
                                                "value": "d8",
                                                "maxValue": 8
                                              }
                                            ]
                                            """)
                            )
                    )
            }
    )
    public Collection<DiceOptionDto> getDices() {
        return dictionariesService.getDices();
    }

    @Operation(summary = "Типы существ")
    @GetMapping("/creature/types")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject("""
                                            [
                                              {
                                                "label": "Аберрация",
                                                "value": "ABERRATION"
                                              },
                                              {
                                                "label": "Зверь",
                                                "value": "BEAST"
                                              },
                                              {
                                                "label": "Небожитель",
                                                "value": "CELESTIAL"
                                              }
                                            ]
                                            """)
                            )
                    )
            }
    )
    public Collection<SelectOptionDto> getCreatureCategories() {
        return dictionariesService.getCreatureCategories();
    }


    @Operation(summary = "Размеры существ")
    @GetMapping("/sizes")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject("""
                                            [
                                              {
                                                "label": "Маленький",
                                                "value": "SMALL"
                                              },
                                              {
                                                "label": "Средний",
                                                "value": "MEDIUM"
                                              },
                                              {
                                                "label": "Большой",
                                                "value": "LARGE"
                                              }
                                            ]
                                            """)
                            )
                    )
            }
    )
    public Collection<SelectOptionDto> getCreatureSizes() {
        return dictionariesService.getCreatureSizes();
    }

    @Operation(summary = "Типы урона")
    @GetMapping("/damage/types")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject("""
                                            [
                                              {
                                                "label": "огонь",
                                                "value": "FAIR"
                                              },
                                              {
                                                "label": "холод",
                                                "value": "COLD"
                                              },
                                              {
                                                "label": "электричество",
                                                "value": "LIGHTNING"
                                              }
                                            ]
                                            """)
                            )
                    )
            }
    )
    public Collection<SelectOptionDto> getDamageTypes() {
        return dictionariesService.getDamageTypes();
    }

    @Operation(summary = "Состояния")
    @GetMapping("/conditions")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject("""
                                            [
                                              {
                                                "label": "ослепление",
                                                "value": "BLINDED"
                                              },
                                              {
                                                "label": "очарование",
                                                "value": "CHARMED"
                                              },
                                              {
                                                "label": "смерть",
                                                "value": "DYING"
                                              }
                                            ]
                                            """)
                            )
                    )
            }
    )
    public Collection<SelectOptionDto> getConditions() {
        return dictionariesService.getConditions();
    }

    @Operation(summary = "Мировоззрение")
    @GetMapping("/alignments")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject("""
                                            [
                                              {
                                                "label": "законно-добрый",
                                                "value": "LAWFUL_GOOD"
                                              },
                                              {
                                                "label": "законно-нейтральный",
                                                "value": "LAWFUL_NEUTRAL"
                                              },
                                              {
                                                "label": "законно-злой",
                                                "value": "LAWFUL_EVIL"
                                              }
                                            ]
                                            """)
                            )
                    )
            }
    )
    public Collection<SelectOptionDto> getAlignments() {
        return dictionariesService.getAlignments();
    }

    @Operation(summary = "Места обитания существ")
    @GetMapping("/environments")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject("""
                                            [
                                              {
                                                "label": "полярная тундра",
                                                "value": "ARCTIC"
                                              },
                                              {
                                                "label": "побережье",
                                                "value": "COAST"
                                              },
                                              {
                                                "label": "под водой",
                                                "value": "WATERS"
                                              }
                                            ]
                                            """)
                            )
                    )
            }
    )
    public Collection<SelectOptionDto> getEnvironments() {
        return dictionariesService.getEnvironments();
    }

    @Operation(summary = "Типы черт")
    @GetMapping("/feat/types")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject("""
                                            [
                                              {
                                                "label": "черты происхождения",
                                                "value": "ORIGIN"
                                              },
                                              {
                                                "label": "общие черты",
                                                "value": "GENERAL"
                                              },
                                              {
                                                "label": "эпические черты",
                                                "value": "EPIC_BOON"
                                              }
                                            ]
                                            """)
                            )
                    )
            }
    )
    public Collection<SelectOptionDto> getFeatTypes() {
        return dictionariesService.getFeatTypes();
    }

    @Operation(summary = "Типы заклинателей")
    @GetMapping("/spellcaster/types")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject("""
                                            [
                                              {
                                                "label": "полный заклинатель",
                                                "value": "FULL",
                                                "levels": 9
                                              },
                                              {
                                                "label": "половинный заклинатель",
                                                "value": "HALF",
                                                "levels": 5
                                              },
                                              {
                                                "label": "частичный заклинатель",
                                                "value": "PARTLY",
                                                "levels": 4
                                              }
                                            ]
                                            """)
                            )
                    )
            }
    )
    public Collection<SpellcasterOptionDto> getSpellcasterTypes() {
        return dictionariesService.getSpellcasterTypes();
    }
}
