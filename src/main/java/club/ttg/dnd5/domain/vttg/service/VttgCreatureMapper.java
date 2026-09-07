package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbility;
import club.ttg.dnd5.domain.beastiary.model.CreatureLair;
import club.ttg.dnd5.domain.beastiary.model.CreatureSection;
import club.ttg.dnd5.domain.beastiary.model.CreatureSkill;
import club.ttg.dnd5.domain.beastiary.model.CreatureSpeeds;
import club.ttg.dnd5.domain.beastiary.model.CreatureTrait;
import club.ttg.dnd5.domain.beastiary.model.action.AttackType;
import club.ttg.dnd5.domain.beastiary.model.action.CreatureAction;
import club.ttg.dnd5.domain.beastiary.model.action.CreatureActionEffect;
import club.ttg.dnd5.domain.beastiary.model.action.SawingThrow;
import club.ttg.dnd5.domain.beastiary.model.language.CreatureLanguage;
import club.ttg.dnd5.domain.beastiary.model.sense.Senses;
import club.ttg.dnd5.domain.beastiary.service.CreatureInitiativeCalculator;
import club.ttg.dnd5.domain.beastiary.model.speed.FlySpeed;
import club.ttg.dnd5.domain.beastiary.model.speed.Speed;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.dictionary.ChallengeRating;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.CreatureTreasure;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.domain.common.dictionary.RechargeType;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.common.model.DamagePart;
import club.ttg.dnd5.domain.spell.model.AreaOfEffect;
import club.ttg.dnd5.domain.spell.model.enums.AreaOfEffectType;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgCreature;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class VttgCreatureMapper {
    private final VttgMarkupConverter markupConverter;
    private final VttgEquipmentMapper equipmentMapper;
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
                .activeEffects(CollectionUtils.isEmpty(creature.getActiveEffects())
                        ? null : creature.getActiveEffects())
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
        int initiativeBonus = CreatureInitiativeCalculator.initiativeBonus(creature);

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
        result.put("initiative", initiative(initiativeBonus));
        result.put("initiativeBonus", initiativeBonus);
        result.put("savingThrows", savingThrows(creature));
        result.put("skills", skills(creature.getSkills()));
        result.put("defenses", defenses(creature));
        result.put("senses", senses(creature));
        result.put("languages", languages(creature));
        putIfHasText(result, "gear", gear(creature));
        putIfNotEmpty(result, "gearItems", equipmentMapper.exportItems(creature.getInventory()));
        result.put("environments", environments(creature));
        result.put("customEnvironments", customEnvironments(creature));
        result.put("traits", traits(creature.getTraits()));
        result.put("actions", actions(creature.getActions()));
        result.put("bonusActions", actions(creature.getBonusActions()));
        result.put("reactions", actions(creature.getReactions()));
        result.put("legendary", legendary(creature));
        if (creature.getLair() != null) {
            result.put("lair", lair(creature));
        }
        putIfNotNull(result, "section", section(creature.getSection()));
        return result;
    }

    /**
     * Инициатива существа в формате редакции 2024: модификатор и пассивное значение.
     * Считается тем же калькулятором, что и карточка сайта, — расходиться этим двум показам нельзя.
     *
     * @param bonus бонус инициативы существа.
     * @return пара «пассивное значение — модификатор» в формате компендиума.
     */
    private Map<String, Object> initiative(int bonus) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("label", String.valueOf(10 + bonus));
        result.put("value", (bonus < 0 ? "" : "+") + bonus);
        return result;
    }

    /**
     * Блок легендарных действий: число за раунд, сами действия и описание блока.
     *
     * @param creature существо.
     * @return блок легендарных действий в формате компендиума.
     */
    private Map<String, Object> legendary(Creature creature) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", creature.getLegendaryAction());
        result.put("actions", actions(creature.getLegendaryActions()));
        putIfHasText(result, "description", text(creature.getLegendaryDescription()));
        return result;
    }

    /**
     * Секция описания существа: подзаголовок, места обитания, сокровища и текст.
     * Пустую секцию не отдаём вовсе — на листе она обернулась бы пустым блоком.
     *
     * @param section секция описания существа.
     * @return секция в формате компендиума или {@code null}, если заполнять нечего.
     */
    private Map<String, Object> section(CreatureSection section) {
        if (section == null) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        putIfHasText(result, "name", section.getSectionName());
        putIfHasText(result, "subtitle", section.getSubtitle());
        putIfHasText(result, "habitats", names(section.getHabitats(), Habitat::getName, ", "));
        putIfHasText(result, "treasures", names(section.getTreasures(), CreatureTreasure::getName, ", "));
        putIfHasText(result, "description", text(section.getSectionDescription()));
        return result.isEmpty() ? null : result;
    }

    /**
     * Особые среды обитания — названия планов. Словарь сред у VTTG короче нашего:
     * все планарные места обитания уезжают туда одним ключом {@code planar}, и конкретный
     * план сохраняется только здесь.
     *
     * @param creature существо.
     * @return названия планов через «; » или пустая строка.
     */
    private String customEnvironments(Creature creature) {
        if (creature.getSection() == null || creature.getSection().getHabitats() == null) {
            return "";
        }
        return value(names(
                creature.getSection().getHabitats().stream()
                        .filter(Objects::nonNull)
                        .filter(habitat -> habitat.name().startsWith("PLANAR_"))
                        .toList(),
                Habitat::getName,
                "; "
        ));
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
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("vulnerabilities", enumNames(creature.getVulnerabilities()));
        result.put("resistances", enumNames(creature.getResistance()));
        result.put("immunities", enumNames(creature.getImmunityToDamage()));
        result.put("conditionImmunities", conditionNames(creature.getImmunityToCondition()));
        // Оговорка статблока («колющий от немагических атак») в ключ защиты не укладывается:
        // на листе она идёт отдельной строкой под значками.
        putIfHasText(result, "vulnerabilitiesText", text(creature.getVulnerabilitiesText()));
        putIfHasText(result, "resistancesText", text(creature.getResistanceText()));
        putIfHasText(result, "immunitiesText", text(creature.getImmunityText()));
        return result;
    }

    private List<Map<String, Object>> traits(Collection<CreatureTrait> traits) {
        if (traits == null) return List.of();
        return traits.stream().filter(Objects::nonNull)
                // Черта тоже бывает бросаемой («Облако слизи» — спасбросок с уроном).
                .map(trait -> entry(trait.getName(), trait.getEnglish(), text(trait.getDescription()),
                        trait.getRecharge(), trait.getEffect(), null, null))
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
                action.getRecharge(), action.getEffect(),
                action.getAttackType(), action.getSawingThrows());
    }

    /**
     * Запись боевого блока существа: черта, действие, реакция, легендарное действие или эффект
     * логова. Всё, чем VTTG кидает бросок, лежит здесь плоскими полями рядом с описанием.
     *
     * <p>В выгрузку идёт ровно то, что заведено в записи. Догадок по тексту описания здесь
     * больше нет: механика существ заполняется в мастерской руками, и разобранное регулярками
     * расходилось с тем, что видит редактор.</p>
     *
     * @param name название записи.
     * @param nameEn английское название записи.
     * @param description описание записи текстом.
     * @param recharge перезарядка записи.
     * @param effect механика записи; {@code null} — её не заводили.
     * @param legacyAttackType тип атаки старого импорта. Лежит рядом с записью, а не внутри
     *                         механики: у записи, которую ни разу не открывали в мастерской,
     *                         механики нет вовсе, а это поле заполнено.
     * @param legacySaves спасброски старого импорта, оттуда же.
     * @return запись в формате компендиума VTTG.
     */
    private Map<String, Object> entry(String name, String nameEn, String description,
                                      RechargeType recharge,
                                      CreatureActionEffect effect,
                                      AttackType legacyAttackType,
                                      Collection<SawingThrow> legacySaves) {
        CreatureActionEffect mechanics = effect == null ? new CreatureActionEffect() : effect;
        AttackType attackType = mechanics.getAttackType() == null
                ? legacyAttackType : mechanics.getAttackType();
        SawingThrow save = first(CollectionUtils.isEmpty(mechanics.getSavingThrows())
                ? legacySaves : mechanics.getSavingThrows());
        boolean hasSave = save != null && save.getAbility() != null;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", value(name));
        putIfHasText(result, "nameEn", nameEn);
        result.put("description", paragraphsFromText(description));
        putIfHasText(result, "recharge", recharge(recharge));

        // Спас заменяет бросок попадания — то же правило, что в форме системы.
        if (!hasSave) {
            putIfNotNull(result, "attackBonus", mechanics.getAttackBonus());
        }

        List<Map<String, Object>> parts = damageParts(mechanics.getDamageParts());
        if (!parts.isEmpty()) {
            result.put("damageParts", parts);
        }

        if (hasSave) {
            result.put("saveType", save.getAbility().name().toLowerCase(Locale.ROOT));
            int dc = Byte.toUnsignedInt(save.getDc());
            if (dc > 0) {
                result.put("saveDC", dc);
            }
            if (mechanics.getSaveEffect() != null) {
                result.put("saveEffect", mechanics.getSaveEffect().name().toLowerCase(Locale.ROOT));
            }
        }

        if (hasArea(mechanics)) {
            result.put("areaOfEffect", areaOfEffect(mechanics.getAreaOfEffect()));
        }

        String rangeType = rangeType(attackType);
        putIfNotNull(result, "reach", mechanics.getReach());
        putIfNotNull(result, "rangeType", rangeType);
        if (mechanics.getReach() != null || mechanics.getRangeNormal() != null || rangeType != null) {
            result.put("distanceUnit", "ft");
        }
        if (mechanics.getRangeNormal() != null) {
            Map<String, Object> range = new LinkedHashMap<>();
            range.put("normal", mechanics.getRangeNormal());
            putIfNotNull(range, "long", mechanics.getRangeLong());
            result.put("range", range);
        }

        if (!CollectionUtils.isEmpty(mechanics.getActiveEffects())) {
            result.put("activeEffects", mechanics.getActiveEffects());
        }
        return result;
    }

    /**
     * Область заведена: у неё выбраны и форма, и размер. Пустой объект области форма шлёт
     * у КАЖДОЙ записи, а размер на ней необязателен — шаблон нулевого размера на столе
     * бесполезен.
     *
     * @param effect механика записи.
     * @return истина, если область можно строить.
     */
    private boolean hasArea(CreatureActionEffect effect) {
        AreaOfEffect area = effect.getAreaOfEffect();
        return area != null && area.getType() != null && area.getValue1() > 0;
    }

    /**
     * Части урона механики в формате компендиума. Часть без формулы — обычное состояние
     * формы, а не значение: в выгрузку она не идёт.
     *
     * @param parts части урона записи.
     * @return части урона для компендиума.
     */
    private List<Map<String, Object>> damageParts(List<DamagePart> parts) {
        if (CollectionUtils.isEmpty(parts)) return List.of();
        return parts.stream()
                .filter(Objects::nonNull)
                .filter(part -> StringUtils.hasText(part.getFormula()))
                .map(part -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("formula", part.getFormula().trim());
                    if (StringUtils.hasText(part.getType())) {
                        result.put("type", part.getType());
                    }
                    if (StringUtils.hasText(part.getTarget())) {
                        result.put("target", part.getTarget());
                    }
                    putIfNotNull(result, "requiresDamage", part.getRequiresDamage());
                    return result;
                })
                .toList();
    }

    /**
     * Область воздействия механики. Формы переводятся той же картой, что у заклинания:
     * словарь сайта шире словаря шаблонов VTTG, и своей карты у существа быть не должно.
     *
     * @param area область воздействия записи.
     * @return область в формате компендиума.
     */
    private Map<String, Object> areaOfEffect(AreaOfEffect area) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("shape", areaShape(area.getType()));
        result.put("size", area.getValue1());
        result.put("unit", "ft");
        if (area.getType() == AreaOfEffectType.LINE) {
            putIfNotNull(result, "width", area.getValue2());
        }
        return result;
    }

    private String areaShape(AreaOfEffectType type) {
        return switch (type) {
            case CONE -> "cone";
            case CUBE -> "rect";
            case LINE -> "ray";
            case CYLINDER, EMANATION, SPHERE -> "circle";
        };
    }

    /**
     * Тип дальности записи по типу атаки. «Рукопашная или дальнобойная» уезжает
     * рукопашной: у неё заполнены и досягаемость, и дальность, а выбрать основную VTTG
     * умеет только одну.
     *
     * @param attackType тип атаки записи.
     * @return {@code melee}, {@code ranged} или {@code null}, если тип не задан.
     */
    private String rangeType(AttackType attackType) {
        if (attackType == null) return null;
        return attackType == AttackType.RANGE ? "ranged" : "melee";
    }

    /**
     * Логово существа. Опыт и число легендарных действий в логове лежат на самом
     * существе, а не в логове: в статблоке это поправки к его собственным числам.
     *
     * @param creature существо с заполненным логовом.
     * @return логово в формате компендиума.
     */
    private Map<String, Object> lair(Creature creature) {
        CreatureLair lair = creature.getLair();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", value(lair.getName()));
        result.put("description", text(lair.getDescription()));
        result.put("effects", actions(lair.getEffects()));
        result.put("ending", text(lair.getEnding()));
        if (creature.getLegendaryActionInLair() > 0) {
            result.put("legendaryActionCount", (int) creature.getLegendaryActionInLair());
        }
        if (creature.getExperienceInLair() != null && creature.getExperienceInLair() > 0) {
            result.put("experience", creature.getExperienceInLair());
        }
        return result;
    }

    private List<String> paragraphsFromText(String text) {
        if (!StringUtils.hasText(text)) return List.of();
        return List.of(text.split("\\R\\s*\\R"));
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

    /**
     * Строка инвентаря. Идёт рядом с позициями, а не вместо них: в ней количества
     * словами («три кинжала») и то, чему карточки на сайте нет, — весом и уроном такая
     * строка не обладает, а позиции обладают.
     *
     * <p>Своей строки нет — берём снаряжение старого импорта: у записей, которым
     * инвентарь ещё не завели, весь текст лежит только там.</p>
     *
     * @param creature существо.
     * @return строка инвентаря или {@code null}, если её нет.
     */
    private String gear(Creature creature) {
        String own = text(creature.getInventoryText());
        return StringUtils.hasText(own) ? own : text(creature.getEquipments());
    }

    private void putIfNotEmpty(Map<String, Object> map, String key, Collection<?> value) {
        if (!CollectionUtils.isEmpty(value)) {
            map.put(key, value);
        }
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private void putIfHasText(Map<String, Object> map, String key, String value) {
        if (StringUtils.hasText(value)) {
            map.put(key, value);
        }
    }

    /**
     * Перезарядка записи ключом словаря VTTG: {@code d5} у «Перезарядка 5–6»,
     * {@code lr} — у «после продолжительного отдыха». Подпись рисует система: словарь
     * показа у неё свой.
     *
     * @param recharge перезарядка записи.
     * @return ключ перезарядки или {@code null}, если её нет.
     */
    private String recharge(RechargeType recharge) {
        return recharge == null ? null : recharge.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Склейка названий справочника в одну строку — статблок системы ждёт здесь текст,
     * а не массив ключей.
     *
     * @param values значения справочника.
     * @param name как взять название у значения.
     * @param separator разделитель.
     * @param <T> тип значения справочника.
     * @return склеенные названия или {@code null}, если значений нет.
     */
    private <T> String names(Collection<T> values, Function<T, String> name, String separator) {
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(name)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.joining(separator));
    }

    private <T> T first(Collection<T> values) {
        return values == null || values.isEmpty() ? null : values.iterator().next();
    }
}
