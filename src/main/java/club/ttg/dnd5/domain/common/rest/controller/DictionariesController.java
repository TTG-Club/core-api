package club.ttg.dnd5.domain.common.rest.controller;

import club.ttg.dnd5.domain.common.rest.dto.select.CrlOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.select.DiceOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.select.KeySelectDto;
import club.ttg.dnd5.domain.common.rest.dto.select.MeasurableSelectOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.select.SelectOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.select.SkillOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.select.SpellcasterOptionDto;
import club.ttg.dnd5.domain.common.service.DictionariesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

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
                                                "label": "ослеплённый",
                                                "value": "BLINDED"
                                              },
                                              {
                                                "label": "отравленный",
                                                "value": "POISONED"
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

    @Operation(summary = "Типы сокровищь")
    @GetMapping("/treasures")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject("""
                                            [
                                              {
                                                "label": "Любые",
                                                "value": "ANY"
                                              },
                                              {
                                                "label": "Индивидуальные",
                                                "value": "INDIVIDUAL"
                                              },
                                              {
                                                "label": "Магия",
                                                "value": "ARCANA"
                                              }
                                            ]
                                            """)
                            )
                    )
            }
    )
    public Collection<SelectOptionDto> getTreasures() {
        return dictionariesService.getTreasures();
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

    @Operation(summary = "Единицы времени")
    @GetMapping("/time-units")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "бонусное действие", "value": "BONUS" },
                              { "label": "реакция", "value": "REACTION" },
                              { "label": "действие", "value": "ACTION" },
                              { "label": "ход", "value": "ROUND" },
                              { "label": "минута", "value": "MINUTE" },
                              { "label": "час", "value": "HOUR" }
                            ]
                            """)
            )
    )
    public ResponseEntity<List<MeasurableSelectOptionDto>> getTimeUnits() {
        return ResponseEntity.ok(dictionariesService.getTimeUnits());
    }

    @Operation(summary = "Единицы времени")
    @GetMapping("/duration-units")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "бонусное действие", "value": "BONUS" },
                              { "label": "реакция", "value": "REACTION" },
                              { "label": "действие", "value": "ACTION" },
                              { "label": "ход", "value": "ROUND" },
                              { "label": "минута", "value": "MINUTE" },
                              { "label": "час", "value": "HOUR" }
                            ]
                            """)
            )
    )
    public ResponseEntity<List<MeasurableSelectOptionDto>> getSpellDurationUnits() {
        return ResponseEntity.ok(dictionariesService.getSpellDurationUnits());
    }

    @Operation(summary = "Школы магии")
    @GetMapping("/magic-schools")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "вызов", "value": "CONJURATION" },
                              { "label": "воплощение", "value": "EVOCATION" },
                              { "label": "иллюзия", "value": "ILLUSION" },
                              { "label": "некромантия", "value": "NECROMANCY" }
                            ]
                            """)
            )
    )
    public ResponseEntity<List<SelectOptionDto>> getMagicSchools() {
        return ResponseEntity.ok(dictionariesService.getMagicSchools());
    }

    @Operation(summary = "Типы дистанций для заклинания")
    @GetMapping("/distance/types")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "на себя", "value": "SELF" },
                              { "label": "касание", "value": "TOUCH" },
                              { "label": "футов", "value": "FEET" },
                              { "label": "в пределах видимости", "value": "SIGHT" }
                            ]
                            """)
            )
    )
    public Collection<MeasurableSelectOptionDto> getDistanceUnits() {
        return dictionariesService.getSpellDistanceUnits();
    }

    @Operation(summary = "Область действия заклинания")
    @GetMapping("/spell-area")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "цилиндр", "value": "CYLINDER" },
                              { "label": "конус", "value": "CONE" },
                              { "label": "куб", "value": "CUBE" },
                              { "label": "эманация", "value": "EMANATION" }
                            ]
                            """)
            )
    )
    public Collection<SelectOptionDto> getSpellAreaOfEffect() {
        return dictionariesService.getSpellAreaOfEffect();
    }

    @Operation(summary = "Операторы сравнения")
    @GetMapping("/comparison-operators")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "<", "value": "LESS" },
                              { "label": ">", "value": "GREATER" },
                              { "label": "=", "value": "EQUAL" }
                            ]
                            """)
            )
    )
    public ResponseEntity<List<SelectOptionDto>> getComparisonOperators() {
        return ResponseEntity.ok(dictionariesService.getComparisonOperators());
    }

    @Operation(summary = "Характеристики")
    @GetMapping("/abilities")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "Сила", "value": "STRENGTH", "key": "str" },
                              { "label": "Ловкость", "value": "DEXTERITY" "key": "dex" },
                              { "label": "Телосложение", "value": "CONSTITUTION" "key": "con" }
                            ]
                            """)
            )
    )
    public Collection<KeySelectDto> getAbilities() {
        return dictionariesService.getAbilities();
    }

    @Operation(summary = "Навыки")
    @GetMapping("/skills")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "Акробатика", "value": "ACROBATICS", "ability" : "DEXTERITY" },
                              { "label": "Уход за животными", "value": "ANIMAL_HANDLING", "ability" : "INTELLIGENCE" },
                              { "label": "Аркана", "value": "ARCANA" }
                            ]
                            """)
            )
    )
    public Collection<SkillOptionDto> getSkills() {
        return dictionariesService.getSkills();
    }

    @Operation(summary = "Дополнительные чувства")
    @GetMapping("/sense-types")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "тёмное зрение", "value": "DARKVISION" },
                              { "label": "слепое зрение", "value": "BLINDSIGHT" },
                              { "label": "истинное зрение", "value": "TRUESIGHT" }
                            ]
                            """)
            )
    )
    public Collection<SelectOptionDto> getSenseType() {
        return dictionariesService.getSenseType();
    }

    @Operation(summary = "Среды обитания")
    @GetMapping("/habitats")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "Любая", "value": "ANY" },
                              { "label": "Арктика", "value": "ARCTIC" },
                              { "label": "Побережье", "value": "COASTAL" }
                            ]
                            """)
            )
    )
    public Collection<SelectOptionDto> getHabitats() {
        return dictionariesService.getHabitats();
    }

    @Operation(summary = "Категории магических предметов")
    @GetMapping("/magic-items/category")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "оружие", "value": "WEAPON" },
                              { "label": "доспех", "value": "ARMOR" },
                              { "label": "волшебная палочка", "value": "WAND" }
                            ]
                            """)
            )
    )
    public Collection<SelectOptionDto> getMagicItemCategories() {
        return dictionariesService.getMagicItemCategories();
    }

    @Operation(summary = "Редкость")
    @GetMapping("/rarity")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "обычный", "value": "COMMON" },
                              { "label": "необычный", "value": "UNCOMMON" },
                              { "label": "редкий", "value": "RARE" }
                            ]
                            """)
            )
    )
    public Collection<SelectOptionDto> getRarities() {
        return dictionariesService.getRarities();
    }

    @Operation(summary = "Типы лечения")
    @GetMapping("/heal/types")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "хиты", "value": "HIT" },
                              { "label": "временные хиты", "value": "TEMPORARY_HIT" },
                            ]
                            """)
            )
    )
    public Collection<SelectOptionDto> getHealTypes() {
        return dictionariesService.getHealTypes();
    }

    @Operation(summary = "Типы чувств")
    @GetMapping("/senses")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "тёмное зрение", "value": "DARKVISION" },
                              { "label": "слепое зрение", "value": "BLINDSIGHT" },
                            ]
                            """)
            )
    )
    public Collection<SelectOptionDto> getSenses() {
        return dictionariesService.getSenses();
    }

    @Operation(summary = "Уровни опасности")
    @GetMapping("/cr")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "0", "value": "10", "pb: 2" },
                              { "label": "1/8", "value": "25", "pb: 2 },
                            ]
                            """)
            )
    )
    public Collection<CrlOptionDto> getChallengeRailings() {
        return dictionariesService.getChallengeRailings();
    }

    @Operation(summary = "Бонус мастерства")
    @GetMapping("/proficiency/bonus")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "0", "value": "2" },
                              { "label": "1/8", "value": "2" },
                            ]
                            """)
            )
    )
    public Collection<SelectOptionDto> getProficiencyBonus() {
        return dictionariesService.getProficiencyBonus();
    }

    @Operation(summary = "Языки (распространенные и экзотические)")
    @GetMapping("/languages")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "общий", "value": "COMMON" },
                              { "label": "драконий", "value": "DRACONIC" },
                            ]
                            """)
            )
    )
    public Collection<SelectOptionDto> getLanguages() {
        return dictionariesService.getLanguages();
    }

    @Operation(summary = "Монеты")
    @GetMapping("/coins")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject("""
                            [
                              { "label": "мм", "value": "CC" },
                              { "label": "см", "value": "SC" },
                              { "label": "зм", "value": "GC" },
                            ]
                            """)
            )
    )
    public Collection<SelectOptionDto> getCoins() {
        return dictionariesService.getCoins();
    }
}
