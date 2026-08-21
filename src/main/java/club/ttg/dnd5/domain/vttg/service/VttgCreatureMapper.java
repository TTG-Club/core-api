package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbility;
import club.ttg.dnd5.domain.beastiary.model.CreatureLair;
import club.ttg.dnd5.domain.beastiary.model.CreatureSkill;
import club.ttg.dnd5.domain.beastiary.model.CreatureSpeeds;
import club.ttg.dnd5.domain.beastiary.model.CreatureTrait;
import club.ttg.dnd5.domain.beastiary.model.action.AttackType;
import club.ttg.dnd5.domain.beastiary.model.action.CreatureAction;
import club.ttg.dnd5.domain.beastiary.model.action.SawingThrow;
import club.ttg.dnd5.domain.beastiary.model.language.CreatureLanguage;
import club.ttg.dnd5.domain.beastiary.model.sense.Senses;
import club.ttg.dnd5.domain.beastiary.model.speed.FlySpeed;
import club.ttg.dnd5.domain.beastiary.model.speed.Speed;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.dictionary.ChallengeRating;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgCreature;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class VttgCreatureMapper {
    private static final Pattern DICE = Pattern.compile(
            "(?iu)(\\d+)\\s*[\\u043akd]\\s*(\\d+)(?:\\s*([+-])\\s*(\\d+))?"
    );
    private static final Pattern TO_HIT = Pattern.compile(
            "(?iu)([+-]?\\d+)\\s*(?:\\u043a\\s+\\u043f\\u043e\\u043f\\u0430\\u0434\\u0430\\u043d\\u0438\\u044e|to\\s+hit)"
    );
    private static final Pattern REACH = Pattern.compile(
            "(?iu)(?:\\u0434\\u043e\\u0441\\u044f\\u0433\\u0430\\u0435\\u043c\\u043e\\u0441\\u0442\\u044c|reach)\\s*(\\d+)\\s*(?:\\u0444\\u0442|ft)"
    );
    private static final Pattern RANGE = Pattern.compile(
            "(?iu)(?:\\u0434\\u0438\\u0441\\u0442\\u0430\\u043d\\u0446\\u0438\\u044f|\\u0434\\u0430\\u043b\\u044c\\u043d\\u043e\\u0431\\u043e\\u0439\\u043d\\u043e\\u0441\\u0442\\u044c|range)\\s*(\\d+)(?:\\s*/\\s*(\\d+))?\\s*(?:\\u0444\\u0442|ft)"
    );
    /**
     * Начало блока с исходом: «*Попадание:*» у атаки и «*Провал:*» у спасброска. Урон разбирается
     * начиная отсюда, иначе в формулу уезжает бросок из соседнего предложения. Закрывающая
     * звёздочка съедается вместе с зачином — иначе плоский урон («*Попадание:* 1 рубящего урона»)
     * не встаёт в начало блока и теряется.
     */
    private static final Pattern HIT_START = Pattern.compile(
            "(?iu)(?:\\u043f\\u043e\\u043f\\u0430\\u0434\\u0430\\u043d\\u0438\\u0435|hit"
                    + "|\\u043f\\u0440\\u043e\\u0432\\u0430\\u043b|failure)\\s*:\\s*\\*?"
    );
    /**
     * Зачин атаки: «*Бросок рукопашной атаки:* +9» (редакция 2024), «Рукопашная атака: +4»
     * и англоязычное «Melee Weapon Attack: +5». Бонус стоит сразу после двоеточия — между
     * ними успевает закрыться звёздочка жирного начертания.
     */
    private static final Pattern ATTACK_ROLL = Pattern.compile(
            "(?iu)(?:бросок\\s+)?"
                    + "(рукопашн\\p{L}*\\s+или\\s+дальнобойн\\p{L}*|рукопашн\\p{L}*|дальнобойн\\p{L}*"
                    + "|melee(?:\\s+or\\s+ranged)?|ranged)"
                    + "\\s+(?:атак\\p{L}*|(?:weapon\\s+)?attack(?:\\s+roll)?)"
                    + "\\s*:\\s*\\*?\\s*\\+?(\\d+)"
    );
    /**
     * Зачин спасброска: «*Спасбросок Ловкости:*», «*Ответ — Спасбросок Мудрости:*» у реакции
     * и «*Спасбросок Мудрости*:», где звёздочка закрывается до двоеточия.
     */
    private static final Pattern SAVE_THROW = Pattern.compile(
            "(?iu)(?:спасбросок\\s+"
                    + "(сил\\p{L}*|ловкост\\p{L}*|телосложени\\p{L}*|интеллект\\p{L}*|мудрост\\p{L}*|харизм\\p{L}*)"
                    + "|(strength|dexterity|constitution|intelligence|wisdom|charisma)\\s+saving\\s+throw)"
                    + "\\s*\\*?\\s*:"
    );
    /** Сложность: в тексте «Сл.» приходит ссылкой на глоссарий, поэтому голого слова мало. */
    private static final Pattern SAVE_DC = Pattern.compile(
            "(?iu)(?:\\[\\s*Сл\\.\\s*\\]\\([^)]*\\)|Сл\\.|DC)\\s*(\\d+)"
    );
    /** Блок «*Успех:*» — по нему различаются {@code half} и {@code special}; без блока это {@code none}. */
    private static final Pattern SUCCESS_BLOCK = Pattern.compile(
            "(?iu)\\*\\s*успех\\s*:\\s*\\*\\s*([^*\\n]{0,60})"
    );
    /** «в [линии](…) длиной 60 фт. и шириной 5 фт.»: длина едет в size, ширина — в width. */
    private static final Pattern AREA_LINE = Pattern.compile(
            "(?iu)лини\\p{L}*(?:\\]\\([^)]*\\))?\\s*длиной\\s*(\\d+)\\s*фт\\.?\\s*и\\s*шириной\\s*(\\d+)\\s*фт"
    );
    /** «в 60-футовом [конусе](…)»: размер стоит перед видом области. */
    private static final Pattern AREA_PREFIXED = Pattern.compile(
            "(?iu)(\\d+)-футов\\p{L}*\\s*\\[?(конус|сфер|цилиндр|эманаци|лини)"
    );
    /** «в [эманации](…) с радиусом 5 фт.», «[цилиндр](…) радиусом 20 фт. и высотой 60 фт.» */
    private static final Pattern AREA_RADIUS = Pattern.compile(
            "(?iu)(конус|сфер|цилиндр|эманаци|лини)\\p{L}*(?:\\]\\([^)]*\\))?\\s*(?:с\\s+)?радиусом\\s*(\\d+)\\s*фт\\.?"
                    + "(?:\\s*и\\s*высотой\\s*(\\d+)\\s*фт)?"
    );
    private static final Pattern FLAT_DAMAGE = Pattern.compile(
            "(?iu)^\\s*(\\d+)\\s+.{0,40}?"
                    + "(?:\\u0443\\u0440\\u043e\\u043d|damage)"
    );
    private static final Pattern DAMAGE_WORD = Pattern.compile("(?iu)(?:\\u0443\\u0440\\u043e\\u043d|damage)");
    private static final Pattern UNICODE_ESCAPE = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
    private static final Map<String, Pattern> TEXT_DAMAGE_TYPES = textDamageTypes();
    private static final List<ConditionEffectTemplate> CONDITION_EFFECTS = conditionEffects();

    private final VttgMarkupConverter markupConverter;
    @Value("${app.url:https://new.ttg.club}")
    private String appUrl = "https://new.ttg.club";

    public VttgCreature toVttg(Creature creature) {
        return VttgCreature.builder()
                .id(creature.getUrl())
                .entityType("creature")
                .type("creature")
                .section("creatures")
                // Раздел сайта у существ — "bestiary", а лист компендиума — "creatures".
                .srcSection(SectionType.BESTIARY.getValue())
                .srcUrl(creature.getUrl())
                .autoSaves(true)
                .name(creature.getName())
                .nameEn(creature.getEnglish())
                .description(text(creature.getDescription()))
                .header(header(creature))
                .token(token(creature))
                .system(system(creature))
                .sourceKey(VttgSourceKeys.of(creature.getSource()))
                .isSRD(creature.getSrdVersion() != null)
                .isReadOnly(true)
                .build();
    }

    private Map<String, Object> token(Creature creature) {
        Size size = first(creature.getSizes() == null ? null : creature.getSizes().getValues());
        Senses senses = creature.getSenses();

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> vision = new LinkedHashMap<>();
        vision.put("range", visionRange(senses));
        vision.put("darkvision", senses == null || senses.getDarkvision() == null ? 0 : senses.getDarkvision().intValue());
        vision.put("angle", 360);
        vision.put("enabled", true);

        result.put("frameUrl", "assets/token-frames/0.png");
        result.put("imageUrl", imageUrl(creature));
        result.put("showName", true);
        result.put("disposition", "hostile");
        result.put("scale", tokenScale(size));
        result.put("vision", vision);
        return result;
    }

    private Number tokenScale(Size size) {
        return switch (size == null ? Size.MEDIUM : size) {
            case TINY -> 0.5;
            case LARGE -> 2;
            case HUGE -> 3;
            case GARGANTUAN -> 4;
            default -> 1;
        };
    }

    private int visionRange(Senses senses) {
        if (senses == null) {
            return 0;
        }
        return Stream.of(
                        senses.getDarkvision(),
                        senses.getBlindsight(),
                        senses.getTruesight(),
                        senses.getTremorsense()
                )
                .filter(Objects::nonNull)
                .mapToInt(Short::intValue)
                .max()
                .orElse(0);
    }

    private String imageUrl(Creature creature) {
        String imageUrl = creature.getImageUrl();
        if (!StringUtils.hasText(imageUrl)) {
            return null;
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }
        return trimTrailingSlash(appUrl) + "/" + trimLeadingSlash(imageUrl);
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String trimLeadingSlash(String value) {
        return value.startsWith("/") ? value.substring(1) : value;
    }

    private Map<String, Object> system(Creature creature) {
        Map<String, Object> result = new LinkedHashMap<>();
        Size size = first(creature.getSizes() == null ? null : creature.getSizes().getValues());
        CreatureType type = first(creature.getTypes() == null ? null : creature.getTypes().getValues());
        long experience = Objects.requireNonNullElse(creature.getExperience(), 0L);
        String challengeRating = ChallengeRating.getCr(experience);

        result.put("size", size == null || size == Size.UNDEFINED ? "medium" : size.name().toLowerCase(Locale.ROOT));
        result.put("type", creatureType(type));
        result.put("subtype", creature.getTypes() == null ? "" : value(creature.getTypes().getText()));
        result.put("alignment", alignment(creature.getAlignment()));
        result.put("armorClass", armorClass(creature));
        result.put("hitPoints", hitPoints(creature, size));
        result.put("movement", movement(creature.getSpeeds()));
        result.put("abilities", abilities(creature));
        result.put("challengeRating", challengeRating);
        result.put("proficiencyBonus", ChallengeRating.getPb(experience));
        result.put("savingThrows", savingThrows(creature));
        result.put("skills", skills(creature.getSkills()));
        result.put("defenses", defenses(creature));
        result.put("senses", senses(creature));
        result.put("languages", languages(creature));
        result.put("environments", environments(creature));
        result.put("customEnvironments", "");
        result.put("traits", traits(creature.getTraits()));
        result.put("actions", actions(creature.getActions()));
        result.put("bonusActions", actions(creature.getBonusActions()));
        result.put("reactions", actions(creature.getReactions()));
        result.put("legendary", Map.of(
                "count", creature.getLegendaryAction(),
                "actions", actions(creature.getLegendaryActions())
        ));
        if (creature.getLair() != null) {
            result.put("lair", lair(creature.getLair()));
        }
        return result;
    }

    private Map<String, Object> armorClass(Creature creature) {
        int value = creature.getArmor() == null ? 10 : creature.getArmor().getArmorClass();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("value", value);
        result.put("calculation", "flat");
        result.put("formula", creature.getArmor() == null ? "" : value(creature.getArmor().getText()));
        result.put("flat", value);
        return result;
    }

    private Map<String, Object> hitPoints(Creature creature, Size size) {
        int average = creature.getHit() == null || creature.getHit().getValue() == null ? 0 : creature.getHit().getValue();
        Integer count = creature.getHit() == null || creature.getHit().getCountHitDice() == null
                ? null : creature.getHit().getCountHitDice().intValue();
        Integer die = size == null || size.getHitDice() == null
                ? null : Integer.parseInt(size.getHitDice().name().substring(1));
        int bonus = count == null || creature.getAbilities() == null || creature.getAbilities().getConstitution() == null
                ? 0 : count * creature.getAbilities().getConstitution().mod();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("average", average);
        result.put("formula", count == null || die == null ? "" : count + "к" + die + signed(bonus));
        result.put("text", creature.getHit() == null ? "" : value(creature.getHit().getText()));
        result.put("current", average);
        result.put("max", average);
        if (die != null) result.put("hitDie", die);
        if (count != null) result.put("hitDiceCount", count);
        result.put("bonus", bonus);
        return result;
    }

    private Map<String, Object> abilities(Creature creature) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (creature.getAbilities() == null) return result;
        result.put("strength", ability(creature.getAbilities().getStrength()));
        result.put("dexterity", ability(creature.getAbilities().getDexterity()));
        result.put("constitution", ability(creature.getAbilities().getConstitution()));
        result.put("intelligence", ability(creature.getAbilities().getIntelligence()));
        result.put("wisdom", ability(creature.getAbilities().getWisdom()));
        result.put("charisma", ability(creature.getAbilities().getCharisma()));
        return result;
    }

    private List<String> savingThrows(Creature creature) {
        if (creature.getAbilities() == null) return List.of();
        return Stream.of(
                        creature.getAbilities().getStrength(),
                        creature.getAbilities().getDexterity(),
                        creature.getAbilities().getConstitution(),
                        creature.getAbilities().getIntelligence(),
                        creature.getAbilities().getWisdom(),
                        creature.getAbilities().getCharisma()
                )
                .filter(Objects::nonNull)
                .filter(ability -> ability.getMultiplier() > 0)
                .map(ability -> ability.getAbility().name().toLowerCase(Locale.ROOT))
                .toList();
    }

    private Map<String, String> skills(Collection<CreatureSkill> skills) {
        Map<String, String> result = new LinkedHashMap<>();
        if (skills == null) return result;
        skills.stream().filter(Objects::nonNull).filter(skill -> skill.getSkill() != null)
                .forEach(skill -> result.put(
                        skill.getSkill().name().toLowerCase(Locale.ROOT).replace("_", "-"),
                        skill.getMultiplier() >= 2 ? "expertise" : "proficient"
                ));
        return result;
    }

    private Map<String, Object> defenses(Creature creature) {
        return Map.of(
                "vulnerabilities", enumNames(creature.getVulnerabilities()),
                "resistances", enumNames(creature.getResistance()),
                "immunities", enumNames(creature.getImmunityToDamage()),
                "conditionImmunities", conditionNames(creature.getImmunityToCondition())
        );
    }

    private List<Map<String, Object>> traits(Collection<CreatureTrait> traits) {
        if (traits == null) return List.of();
        return traits.stream().filter(Objects::nonNull)
                // Черта тоже бывает бросаемой («Облако слизи» — спасбросок с уроном), а
                // структурированных полей у неё нет вовсе: всё берётся из описания.
                .map(trait -> entry(trait.getName(), trait.getEnglish(), text(trait.getDescription()),
                        null, null, null))
                .toList();
    }

    private List<Map<String, Object>> actions(Collection<CreatureAction> actions) {
        if (actions == null) return List.of();
        return actions.stream().filter(Objects::nonNull)
                .map(this::action)
                .toList();
    }

    private Map<String, Object> action(CreatureAction action) {
        return entry(action.getName(), action.getEnglish(), text(action.getDescription()),
                action.getAttackType(), first(action.getSawingThrows()), action.getDamageTypes());
    }

    /**
     * Запись боевого блока существа: черта, действие, реакция, легендарное действие или эффект
     * логова. Всё, чем VTTG кидает бросок, лежит здесь плоскими полями рядом с описанием.
     */
    private Map<String, Object> entry(String name, String nameEn, String description,
                                      AttackType attackType, SawingThrow savingThrow,
                                      Collection<DamageType> damageTypes) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", value(name));
        putIfHasText(result, nameEn);
        result.put("description", paragraphsFromText(description));

        CreatureActionMechanics mechanics = extractActionMechanics(attackType, savingThrow, damageTypes, description);
        putIfNotNull(result, "attackBonus", mechanics.attackBonus());
        if (mechanics.damageFormula() != null) {
            Map<String, Object> damagePart = new LinkedHashMap<>();
            damagePart.put("formula", mechanics.damageFormula());
            putIfNotNull(damagePart, "type", mechanics.damageType());
            result.put("damageParts", List.of(damagePart));
        }
        putIfNotNull(result, "saveType", mechanics.saveType());
        putIfNotNull(result, "saveDC", mechanics.saveDC());
        putIfNotNull(result, "saveEffect", mechanics.saveEffect());
        if (mechanics.area() != null) {
            result.put("areaOfEffect", areaOfEffect(mechanics.area()));
        }
        putIfNotNull(result, "reach", mechanics.reach());
        putIfNotNull(result, "rangeType", mechanics.rangeType());
        if (mechanics.reach() != null || mechanics.range() != null || mechanics.rangeType() != null) {
            result.put("distanceUnit", "ft");
        }
        if (mechanics.range() != null) {
            Map<String, Object> range = new LinkedHashMap<>();
            range.put("normal", mechanics.range());
            if (mechanics.longRange() != null) {
                range.put("long", mechanics.longRange());
            }
            result.put("range", range);
        }
        List<Map<String, Object>> activeEffects = activeEffects(description, mechanics.riderSave());
        if (!activeEffects.isEmpty()) {
            result.put("activeEffects", activeEffects);
        }
        return result;
    }

    private Map<String, Object> areaOfEffect(AreaValues area) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("shape", area.shape());
        result.put("size", area.size());
        result.put("unit", "ft");
        putIfNotNull(result, "width", area.width());
        putIfNotNull(result, "height", area.height());
        return result;
    }

    private Map<String, Object> lair(CreatureLair lair) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", value(lair.getName()));
        result.put("description", text(lair.getDescription()));
        result.put("effects", actions(lair.getEffects()));
        result.put("ending", text(lair.getEnding()));
        return result;
    }

    private List<String> paragraphsFromText(String text) {
        if (!StringUtils.hasText(text)) return List.of();
        return List.of(text.split("\\R\\s*\\R"));
    }

    private CreatureActionMechanics extractActionMechanics(AttackType attackTypeSource, SawingThrow savingThrow,
                                                           Collection<DamageType> damageTypes, String description) {
        String text = value(description);
        Matcher attackRoll = ATTACK_ROLL.matcher(text);
        String attackKind = attackRoll.find() ? attackRoll.group(1) : null;
        boolean hasHitText = HIT_START.matcher(text).find();
        Integer attackBonus = attackKind == null
                // Старая запись «+5 к попаданию»: бонус стоит перед оборотом, а не после зачина.
                // Блок исхода обязателен — без него тот же оборот встречается в «Использовании
                // заклинаний», где это бонус заклинательства, а не атака оружием.
                ? (hasHitText ? firstInt(TO_HIT.matcher(text)) : null)
                : Integer.valueOf(attackRoll.group(2));
        String attackType = attackType(attackKind, attackTypeSource, attackBonus, text);
        Integer reach = firstInt(REACH.matcher(text));
        RangeValues range = range(text);
        SaveValues detected = save(savingThrow, text);
        // Спас заменяет бросок попадания: и saveType, и areaOfEffect переключают запись на
        // бросок урона без шанса промаха (usesSaveOrArea в CreatureActionsBlock). Поэтому у
        // записи с бонусом атаки спас едет не в saveType, а довеском на сами эффекты.
        SaveValues save = attackBonus == null ? detected : null;
        SaveValues riderSave = attackBonus == null ? null : detected;
        AreaValues area = attackBonus == null ? area(text) : null;
        boolean attackLike = attackType != null || attackBonus != null || hasHitText || detected != null;
        String hitText = hitText(text);
        List<DamageSegment> damageSegments = attackLike ? damageSegments(hitText) : List.of();
        String damageFormula;
        String damageType;
        if (damageSegments.stream().filter(segment -> segment.type() != null).count() >= 2) {
            // Несколько типов урона: тип кодируется токенами @dmg.<type> прямо в формуле.
            damageFormula = combinedDamageFormula(damageSegments);
            damageType = null;
        } else {
            damageFormula = attackLike ? damageDice(hitText) : null;
            damageType = damageType(damageTypes, hitText, damageFormula);
        }

        return new CreatureActionMechanics(
                attackType,
                attackBonus,
                damageFormula,
                damageType,
                save == null ? null : save.ability(),
                save == null ? null : save.dc(),
                save == null ? null : save.effect(),
                riderSave,
                area,
                reach,
                range == null ? null : range.normal(),
                range == null ? null : range.longRange()
        );
    }

    /**
     * Вид броска. Зачин из описания точнее всего: он же несёт бонус. Без зачина остаётся
     * структурированное поле, а совсем без него — ключевые слова, но только у записи с бонусом
     * атаки: иначе «дальнобойн» из любой черты навесило бы ей дистанционный бросок.
     */
    private String attackType(String attackKind, AttackType source, Integer attackBonus, String text) {
        if (attackKind != null) {
            String kind = attackKind.toLowerCase(Locale.ROOT);
            // «Рукопашная или дальнобойная» — бросок совершается как рукопашный.
            return kind.startsWith("рукопашн") || kind.startsWith("melee") ? "melee" : "ranged";
        }
        if (source == AttackType.MELEE || source == AttackType.MELEE_OR_RANGE) {
            return "melee";
        }
        if (source == AttackType.RANGE) {
            return "ranged";
        }
        if (attackBonus == null) {
            return null;
        }
        String lower = value(text).toLowerCase(Locale.ROOT);
        if (lower.contains("melee") || lower.contains("рукопашн")) {
            return "melee";
        }
        if (lower.contains("ranged") || lower.contains("дальнобойн")) {
            return "ranged";
        }
        return null;
    }

    /**
     * Спасбросок записи. Структурированное поле в приоритете, но у существ редакции 2024 оно
     * пустое: характеристика и Сл. остаются только в описании, причём «Сл.» приезжает ссылкой
     * на глоссарий.
     */
    private SaveValues save(SawingThrow structured, String text) {
        if (structured != null && structured.getAbility() != null) {
            return new SaveValues(
                    structured.getAbility().name().toLowerCase(Locale.ROOT),
                    Byte.toUnsignedInt(structured.getDc()),
                    saveEffect(text),
                    -1
            );
        }
        Matcher matcher = SAVE_THROW.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        String ability = ability(matcher.group(1) == null ? matcher.group(2) : matcher.group(1));
        if (ability == null) {
            return null;
        }
        Matcher dc = SAVE_DC.matcher(text);
        return new SaveValues(
                ability,
                dc.find(matcher.end()) ? Integer.valueOf(dc.group(1)) : null,
                saveEffect(text),
                matcher.start()
        );
    }

    private String ability(String word) {
        String lower = value(word).toLowerCase(Locale.ROOT);
        if (lower.startsWith("сил") || lower.startsWith("strength")) return "strength";
        if (lower.startsWith("лов") || lower.startsWith("dexterity")) return "dexterity";
        if (lower.startsWith("тел") || lower.startsWith("constitution")) return "constitution";
        if (lower.startsWith("инт") || lower.startsWith("intelligence")) return "intelligence";
        if (lower.startsWith("муд") || lower.startsWith("wisdom")) return "wisdom";
        if (lower.startsWith("хар") || lower.startsWith("charisma")) return "charisma";
        return null;
    }

    /**
     * Область эффекта. Три записи размера: «в линии длиной N и шириной M», «в N-футовом конусе»
     * и «в эманации с радиусом N». Берётся самая ранняя — она относится к самому эффекту, а не
     * к дистанции до цели в продолжении фразы.
     */
    private AreaValues area(String text) {
        AreaValues result = null;
        int start = Integer.MAX_VALUE;
        Matcher line = AREA_LINE.matcher(text);
        if (line.find()) {
            result = new AreaValues("line", Integer.parseInt(line.group(1)), Integer.parseInt(line.group(2)), null);
            start = line.start();
        }
        Matcher prefixed = AREA_PREFIXED.matcher(text);
        if (prefixed.find() && prefixed.start() < start) {
            result = new AreaValues(areaShape(prefixed.group(2)), Integer.parseInt(prefixed.group(1)), null, null);
            start = prefixed.start();
        }
        Matcher radius = AREA_RADIUS.matcher(text);
        if (radius.find() && radius.start() < start) {
            result = new AreaValues(
                    areaShape(radius.group(1)),
                    Integer.parseInt(radius.group(2)),
                    null,
                    radius.group(3) == null ? null : Integer.valueOf(radius.group(3))
            );
        }
        return result;
    }

    private String areaShape(String word) {
        return switch (word.toLowerCase(Locale.ROOT)) {
            case "сфер" -> "sphere";
            case "цилиндр" -> "cylinder";
            case "эманаци" -> "emanation";
            case "лини" -> "line";
            default -> "cone";
        };
    }

    private RangeValues range(String text) {
        Matcher matcher = RANGE.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        return new RangeValues(
                Integer.parseInt(matcher.group(1)),
                StringUtils.hasText(matcher.group(2)) ? Integer.parseInt(matcher.group(2)) : null
        );
    }

    private String hitText(String text) {
        Matcher matcher = HIT_START.matcher(text);
        return matcher.find() ? text.substring(matcher.end()) : text;
    }

    private String damageDice(String text) {
        String damageContext = firstDamageContext(text);
        Matcher matcher = DICE.matcher(damageContext);
        if (!matcher.find()) {
            return firstString(FLAT_DAMAGE.matcher(text));
        }
        String formula = matcher.group(1) + "к" + matcher.group(2);
        if (StringUtils.hasText(matcher.group(3))) {
            formula += " " + matcher.group(3) + " " + matcher.group(4);
        }
        return formula;
    }

    /**
     * Разбивает текст попадания на отдельные сегменты урона. Каждый сегмент привязан
     * к своей формуле костей; область поиска типа урона ограничена следующим броском,
     * поэтому типы из соседних сегментов не смешиваются.
     */
    private List<DamageSegment> damageSegments(String hitText) {
        Matcher matcher = DICE.matcher(hitText);
        List<Integer> starts = new ArrayList<>();
        List<String> formulas = new ArrayList<>();
        while (matcher.find()) {
            starts.add(matcher.start());
            String formula = matcher.group(1) + "к" + matcher.group(2);
            if (StringUtils.hasText(matcher.group(3))) {
                formula += matcher.group(3) + matcher.group(4);
            }
            formulas.add(formula);
        }
        List<DamageSegment> result = new ArrayList<>();
        for (int i = 0; i < starts.size(); i++) {
            int start = starts.get(i);
            int end = i + 1 < starts.size() ? starts.get(i + 1) : hitText.length();
            String region = hitText.substring(start, end);
            if (!DAMAGE_WORD.matcher(region).find()) {
                continue;
            }
            result.add(new DamageSegment(formulas.get(i), damageTypeIn(region)));
        }
        return result;
    }

    private String damageTypeIn(String region) {
        String lower = region.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Pattern> entry : TEXT_DAMAGE_TYPES.entrySet()) {
            if (entry.getValue().matcher(lower).find()) {
                return entry.getKey();
            }
        }
        return null;
    }

    private String combinedDamageFormula(List<DamageSegment> segments) {
        return segments.stream()
                .map(segment -> segment.type() == null
                        ? segment.formula()
                        : segment.formula() + "@dmg." + segment.type())
                .collect(Collectors.joining("+"));
    }

    /**
     * Окрестность слова «урон». Берётся первая, где рядом стоит бросок: слово встречается и в
     * оговорках вроде «пока не получит урон», а кости с типом урона — уже в следующем предложении.
     */
    private String firstDamageContext(String text) {
        Matcher matcher = DAMAGE_WORD.matcher(text);
        String first = null;
        while (matcher.find()) {
            String context = window(text, matcher.start() - 80, matcher.end() + 40);
            if (DICE.matcher(context).find()) {
                return context;
            }
            if (first == null) {
                first = context;
            }
        }
        return first == null ? text : first;
    }

    private String damageType(Collection<DamageType> damageTypes, String text, String damageFormula) {
        if (!StringUtils.hasText(damageFormula)) {
            return null;
        }
        String lower = firstDamageContext(text).toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Pattern> entry : TEXT_DAMAGE_TYPES.entrySet()) {
            if (entry.getValue().matcher(lower).find()) {
                return entry.getKey();
            }
        }
        DamageType structuredType = first(damageTypes);
        return structuredType == null ? null : damageType(structuredType);
    }

    /**
     * Исход при успехе. У записи редакции 2024 он вынесен в блок «*Успех:*»: «половина урона»
     * даёт {@code half}, любая другая формулировка — {@code special}. Блока нет — при успехе
     * не происходит ничего, то есть {@code none}.
     */
    private String saveEffect(String text) {
        Matcher success = SUCCESS_BLOCK.matcher(text);
        if (success.find()) {
            return success.group(1).trim().toLowerCase(Locale.ROOT).startsWith("половина урона")
                    ? "half" : "special";
        }
        String lower = value(text).toLowerCase(Locale.ROOT);
        if (lower.contains("half as much") || lower.contains("half damage")
                || lower.contains("половин")) {
            return "half";
        }
        return "none";
    }

    /**
     * Состояния, которые запись накладывает на цель. Спас-довесок атаки гейтит только те из
     * них, что описаны ПОСЛЕ его зачина: «*Попадание:* и цель отравлена» вешается без броска,
     * а «*Провал:* цель парализована» — лишь при провале. Без {@code applySave} такое состояние
     * ложилось бы на цель безусловно, ведь на уровне записи спасброска у атаки нет.
     */
    private List<Map<String, Object>> activeEffects(String description, SaveValues riderSave) {
        String text = value(description);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ConditionEffectTemplate template : CONDITION_EFFECTS) {
            Matcher matcher = template.pattern().matcher(text);
            if (!matcher.find()) {
                continue;
            }
            boolean gated = riderSave != null && riderSave.dc() != null && matcher.start() > riderSave.start();
            result.add(activeEffect(template, gated ? riderSave : null));
        }
        return result;
    }

    private Map<String, Object> activeEffect(ConditionEffectTemplate template, SaveValues applySave) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", template.id());
        result.put("name", template.name());
        result.put("description", template.description());
        result.put("icon", template.icon());
        result.put("disabled", false);
        result.put("origin", "feature");
        result.put("transfer", false);
        result.put("effectTarget", "target");
        if (applySave != null) {
            Map<String, Object> save = new LinkedHashMap<>();
            save.put("ability", applySave.ability());
            save.put("dc", applySave.dc());
            // Состояние либо ложится, либо нет: половинить у него нечего — урон несёт сама атака.
            save.put("onSuccess", "negate");
            result.put("applySave", save);
        }
        result.put("duration", Map.of("type", "special"));
        result.put("changes", List.of());
        result.put("flags", template.flags());
        return result;
    }

    private Integer firstInt(Matcher matcher) {
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    private String firstString(Matcher matcher) {
        return matcher.find() ? matcher.group(1) : null;
    }

    private Map<String, Object> movement(CreatureSpeeds speeds) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("walk", speed(first(speeds == null ? null : speeds.getWalk())));
        result.put("swim", speed(first(speeds == null ? null : speeds.getSwim())));
        result.put("fly", speed(first(speeds == null ? null : speeds.getFly())));
        result.put("climb", speed(first(speeds == null ? null : speeds.getClimb())));
        result.put("burrow", speed(first(speeds == null ? null : speeds.getBurrow())));
        FlySpeed fly = first(speeds == null ? null : speeds.getFly());
        result.put("hover", fly != null && fly.isHover());
        result.put("units", "ft");
        return result;
    }

    private String senses(Creature creature) {
        if (creature.getSenses() == null) return "";
        List<String> values = new ArrayList<>();
        if (creature.getSenses().getDarkvision() != null) values.add("тёмное зрение " + creature.getSenses().getDarkvision() + " фт.");
        if (creature.getSenses().getBlindsight() != null) values.add("слепое зрение " + creature.getSenses().getBlindsight() + " фт.");
        if (creature.getSenses().getTruesight() != null) values.add("истинное зрение " + creature.getSenses().getTruesight() + " фт.");
        if (creature.getSenses().getTremorsense() != null) values.add("чувство вибрации " + creature.getSenses().getTremorsense() + " фт.");
        values.add("пассивная Внимательность " + creature.getSenses().getPassivePerception());
        return String.join(", ", values);
    }

    private List<String> languages(Creature creature) {
        if (creature.getLanguages() == null || creature.getLanguages().getValues() == null) return List.of();
        return creature.getLanguages().getValues().stream().filter(Objects::nonNull)
                .map(CreatureLanguage::getLanguage).filter(Objects::nonNull)
                .map(language -> language.name().toLowerCase(Locale.ROOT).replace("_", "-"))
                .toList();
    }

    private List<String> environments(Creature creature) {
        if (creature.getSection() == null || creature.getSection().getHabitats() == null) {
            return List.of();
        }
        return creature.getSection().getHabitats().stream()
                .filter(Objects::nonNull)
                .map(this::environment)
                .distinct()
                .toList();
    }

    private String environment(Habitat habitat) {
        return habitat.name().startsWith("PLANAR_")
                ? "planar"
                : habitat.name().toLowerCase(Locale.ROOT);
    }

    private String header(Creature creature) {
        Size size = first(creature.getSizes() == null ? null : creature.getSizes().getValues());
        CreatureType type = first(creature.getTypes() == null ? null : creature.getTypes().getValues());
        String sizeName = size == null ? "" : size.getSizeName(type == null ? CreatureType.HUMANOID : type);
        String typeName = type == null ? "" : type.getName();
        CreatureType headerType = type == null ? CreatureType.HUMANOID : type;
        String alignment = creature.getAlignment() == null ? "" : creature.getAlignment().getName(headerType);
        return String.join(", ", List.of(sizeName + " " + typeName, alignment));
    }

    private String creatureType(CreatureType type) {
        if (type == null) return "humanoid";
        if (type == CreatureType.SLIME) return "ooze";
        String name = type.name();
        if (name.startsWith("SWARM_OF_")) name = name.substring(name.lastIndexOf('_') + 1);
        if (name.endsWith("S")) name = name.substring(0, name.length() - 1);
        return name.toLowerCase(Locale.ROOT);
    }

    private String alignment(Alignment alignment) {
        if (alignment == null || alignment == Alignment.WITHOUT) return "unaligned";
        if (alignment == Alignment.NEUTRAL) return "true-neutral";
        return alignment.name().toLowerCase(Locale.ROOT).replace("_", "-");
    }


    private List<String> enumNames(Collection<DamageType> values) {
        return values == null ? List.of() : values.stream().filter(Objects::nonNull)
                .map(this::damageType)
                .toList();
    }

    private String damageType(DamageType value) {
        return value.name().toLowerCase(Locale.ROOT);
    }

    private List<String> conditionNames(Collection<Condition> values) {
        return values == null ? List.of() : values.stream().filter(Objects::nonNull)
                .map(value -> value.name().toLowerCase(Locale.ROOT)).toList();
    }

    private int ability(CreatureAbility ability) {
        return ability == null ? 10 : ability.getValue();
    }

    private int speed(Speed speed) {
        return speed == null ? 0 : speed.getValue();
    }

    private String text(String markup) {
        String result = markupConverter.toText(markup);
        return StringUtils.hasText(result) ? result : null;
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private String signed(int value) {
        if (value == 0) return "";
        return value > 0 ? " + " + value : " - " + Math.abs(value);
    }

    private String window(String text, int start, int end) {
        return text.substring(Math.max(0, start), Math.min(text.length(), end));
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private void putIfHasText(Map<String, Object> map, String value) {
        if (StringUtils.hasText(value)) {
            map.put("nameEn", value);
        }
    }

    private <T> T first(Collection<T> values) {
        return values == null || values.isEmpty() ? null : values.iterator().next();
    }

    private static Map<String, Pattern> textDamageTypes() {
        Map<String, Pattern> result = new LinkedHashMap<>();
        result.put("acid", Pattern.compile("(?iu)\\u043a\\u0438\\u0441\\u043b\\u043e\\u0442|acid"));
        result.put("bludgeoning", Pattern.compile("(?iu)\\u0434\\u0440\\u043e\\u0431\\u044f\\u0449|bludgeoning"));
        result.put("cold", Pattern.compile("(?iu)\\u0445\\u043e\\u043b\\u043e\\u0434|cold"));
        result.put("fire", Pattern.compile("(?iu)\\u043e\\u0433\\u043d|\\u043f\\u043b\\u0430\\u043c\\u0435\\u043d|fire"));
        result.put("force", Pattern.compile("(?iu)\\u0441\\u0438\\u043b\\u043e\\u0432|force"));
        result.put("lightning", Pattern.compile("(?iu)\\u044d\\u043b\\u0435\\u043a\\u0442\\u0440|\\u043c\\u043e\\u043b\\u043d\\u0438|lightning"));
        result.put("necrotic", Pattern.compile("(?iu)\\u043d\\u0435\\u043a\\u0440\\u043e\\u0442|necrotic"));
        result.put("piercing", Pattern.compile("(?iu)\\u043a\\u043e\\u043b\\u044e\\u0449|piercing"));
        result.put("poison", Pattern.compile("(?iu)\\u044f\\u0434|poison"));
        result.put("psychic", Pattern.compile("(?iu)\\u043f\\u0441\\u0438\\u0445\\u0438\\u0447|psychic"));
        result.put("radiant", Pattern.compile("(?iu)\\u0438\\u0437\\u043b\\u0443\\u0447\\u0435\\u043d|\\u0441\\u0438\\u044f\\u044e\\u0449|radiant"));
        result.put("slashing", Pattern.compile("(?iu)\\u0440\\u0443\\u0431\\u044f\\u0449|slashing"));
        result.put("thunder", Pattern.compile("(?iu)\\u0437\\u0432\\u0443\\u043a|\\u0433\\u0440\\u043e\\u043c|thunder"));
        return result;
    }

    private static List<ConditionEffectTemplate> conditionEffects() {
        return List.of(
                conditionEffect(
                        "poisoned",
                        "\\u041e\\u0442\\u0440\\u0430\\u0432\\u043b\\u0435\\u043d\\u043d\\u044b\\u0439",
                        "\\u0426\\u0435\\u043b\\u044c \\u043e\\u0442\\u0440\\u0430\\u0432\\u043b\\u0435\\u043d\\u0430.",
                        "tabler:biohazard",
                        "(?:poisoned|\\u043e\\u0442\\u0440\\u0430\\u0432\\u043b\\u0435\\u043d\\p{L}*)",
                        List.of("attack.disadvantage", "abilityCheck.disadvantage")
                ),
                conditionEffect(
                        "paralyzed",
                        "\\u041f\\u0430\\u0440\\u0430\\u043b\\u0438\\u0437\\u043e\\u0432\\u0430\\u043d\\u043d\\u044b\\u0439",
                        "\\u0426\\u0435\\u043b\\u044c \\u043f\\u0430\\u0440\\u0430\\u043b\\u0438\\u0437\\u043e\\u0432\\u0430\\u043d\\u0430.",
                        "tabler:user-off",
                        "(?:paralyzed|\\u043f\\u0430\\u0440\\u0430\\u043b\\u0438\\u0437\\u043e\\u0432\\u0430\\u043d\\p{L}*)",
                        List.of(
                                "incapacitated",
                                "speed.zero",
                                "save.autoFail.strength",
                                "save.autoFail.dexterity",
                                "attacksAgainst.advantage"
                        )
                ),
                conditionEffect(
                        "restrained",
                        "\\u041e\\u043f\\u0443\\u0442\\u0430\\u043d\\u043d\\u044b\\u0439",
                        "\\u0426\\u0435\\u043b\\u044c \\u043e\\u043f\\u0443\\u0442\\u0430\\u043d\\u0430.",
                        "tabler:link",
                        "(?:restrained|\\u043e\\u043f\\u0443\\u0442\\u0430\\u043d\\p{L}*)",
                        List.of("speed.zero", "attack.disadvantage", "attacksAgainst.advantage")
                ),
                conditionEffect(
                        "grappled",
                        "\\u0421\\u0445\\u0432\\u0430\\u0447\\u0435\\u043d\\u043d\\u044b\\u0439",
                        "\\u0426\\u0435\\u043b\\u044c \\u0441\\u0445\\u0432\\u0430\\u0447\\u0435\\u043d\\u0430.",
                        "tabler:hand-grab",
                        "(?:grappled|\\u0441\\u0445\\u0432\\u0430\\u0447\\u0435\\u043d\\p{L}*)",
                        List.of("speed.zero")
                ),
                conditionEffect(
                        "prone",
                        "\\u0421\\u0431\\u0438\\u0442\\u044b\\u0439 \\u0441 \\u043d\\u043e\\u0433",
                        "\\u0426\\u0435\\u043b\\u044c \\u0441\\u0431\\u0438\\u0442\\u0430 \\u0441 \\u043d\\u043e\\u0433.",
                        "tabler:walk",
                        "(?:prone|\\u0441\\u0431\\u0438\\u0442\\p{L}*\\s+\\u0441\\s+\\u043d\\u043e\\u0433|\\u043f\\u0430\\u0434\\u0430\\u0435\\u0442\\s+\\u043d\\u0438\\u0447\\u043a\\u043e\\u043c)",
                        List.of("attack.disadvantage")
                ),
                conditionEffect(
                        "frightened",
                        "\\u0418\\u0441\\u043f\\u0443\\u0433\\u0430\\u043d\\u043d\\u044b\\u0439",
                        "\\u0426\\u0435\\u043b\\u044c \\u0438\\u0441\\u043f\\u0443\\u0433\\u0430\\u043d\\u0430.",
                        "tabler:ghost",
                        "(?:frightened|\\u0438\\u0441\\u043f\\u0443\\u0433\\u0430\\u043d\\p{L}*)",
                        List.of("attack.disadvantage", "abilityCheck.disadvantage")
                ),
                conditionEffect(
                        "blinded",
                        "\\u041e\\u0441\\u043b\\u0435\\u043f\\u043b\\u0435\\u043d\\u043d\\u044b\\u0439",
                        "\\u0426\\u0435\\u043b\\u044c \\u043e\\u0441\\u043b\\u0435\\u043f\\u043b\\u0435\\u043d\\u0430.",
                        "tabler:eye-off",
                        "(?:blinded|\\u043e\\u0441\\u043b\\u0435\\u043f\\u043b\\u0435\\u043d\\p{L}*)",
                        List.of("vision.blinded", "attack.disadvantage", "attacksAgainst.advantage")
                ),
                conditionEffect(
                        "unconscious",
                        "\\u0411\\u0435\\u0441\\u0441\\u043e\\u0437\\u043d\\u0430\\u0442\\u0435\\u043b\\u044c\\u043d\\u044b\\u0439",
                        "\\u0426\\u0435\\u043b\\u044c \\u0431\\u0435\\u0437 \\u0441\\u043e\\u0437\\u043d\\u0430\\u043d\\u0438\\u044f.",
                        "tabler:zzz",
                        "(?:unconscious|\\u0431\\u0435\\u0437\\s+\\u0441\\u043e\\u0437\\u043d\\u0430\\u043d\\u0438\\u044f|\\u0431\\u0435\\u0441\\u0441\\u043e\\u0437\\u043d\\u0430\\u0442\\u0435\\u043b\\p{L}*)",
                        List.of(
                                "incapacitated",
                                "speed.zero",
                                "save.autoFail.strength",
                                "save.autoFail.dexterity",
                                "attacksAgainst.advantage"
                        )
                ),
                conditionEffect(
                        "stunned",
                        "\\u041e\\u0448\\u0435\\u043b\\u043e\\u043c\\u043b\\u0435\\u043d\\u043d\\u044b\\u0439",
                        "\\u0426\\u0435\\u043b\\u044c \\u043e\\u0448\\u0435\\u043b\\u043e\\u043c\\u043b\\u0435\\u043d\\u0430.",
                        "tabler:stars",
                        "(?:stunned|\\u043e\\u0448\\u0435\\u043b\\u043e\\u043c\\u043b\\u0435\\u043d\\p{L}*)",
                        List.of(
                                "incapacitated",
                                "speed.zero",
                                "save.autoFail.strength",
                                "save.autoFail.dexterity",
                                "attacksAgainst.advantage"
                        )
                ),
                conditionEffect(
                        "incapacitated",
                        "\\u041d\\u0435\\u0434\\u0435\\u0435\\u0441\\u043f\\u043e\\u0441\\u043e\\u0431\\u043d\\u044b\\u0439",
                        "\\u0426\\u0435\\u043b\\u044c \\u043d\\u0435\\u0434\\u0435\\u0435\\u0441\\u043f\\u043e\\u0441\\u043e\\u0431\\u043d\\u0430.",
                        "tabler:ban",
                        "(?:incapacitated|\\u043d\\u0435\\u0434\\u0435\\u0435\\u0441\\u043f\\u043e\\u0441\\u043e\\u0431\\p{L}*)",
                        List.of("incapacitated")
                )
        );
    }

    private static ConditionEffectTemplate conditionEffect(
            String id, String name, String description, String icon, String pattern, List<String> flags) {
        return new ConditionEffectTemplate(
                id,
                decodeUnicodeEscapes(name),
                decodeUnicodeEscapes(description),
                icon,
                Pattern.compile("(?iu)" + pattern),
                flags
        );
    }

    private static String decodeUnicodeEscapes(String value) {
        Matcher matcher = UNICODE_ESCAPE.matcher(value);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(
                    Character.toString((char) Integer.parseInt(matcher.group(1), 16))
            ));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private record RangeValues(Integer normal, Integer longRange) {
    }

    private record DamageSegment(String formula, String type) {
    }

    /**
     * @param start позиция зачина спасброска в описании; {@code -1} у структурированного поля,
     *              где текстовой привязки нет
     */
    private record SaveValues(String ability, Integer dc, String effect, int start) {
    }

    private record AreaValues(String shape, int size, Integer width, Integer height) {
    }

    private record CreatureActionMechanics(
            String rangeType,
            Integer attackBonus,
            String damageFormula,
            String damageType,
            String saveType,
            Integer saveDC,
            String saveEffect,
            SaveValues riderSave,
            AreaValues area,
            Integer reach,
            Integer range,
            Integer longRange) {
    }

    private record ConditionEffectTemplate(
            String id,
            String name,
            String description,
            String icon,
            Pattern pattern,
            List<String> flags) {
    }
}
