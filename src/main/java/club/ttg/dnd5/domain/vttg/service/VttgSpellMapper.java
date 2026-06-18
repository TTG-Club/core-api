package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.beastiary.model.action.AttackType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.spell.model.AreaOfEffect;
import club.ttg.dnd5.domain.spell.model.MaterialComponent;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.model.SpellCastingTime;
import club.ttg.dnd5.domain.spell.model.SpellComponents;
import club.ttg.dnd5.domain.spell.model.SpellDistance;
import club.ttg.dnd5.domain.spell.model.SpellDuration;
import club.ttg.dnd5.domain.spell.model.SpellEffect;
import club.ttg.dnd5.domain.spell.model.enums.AreaOfEffectType;
import club.ttg.dnd5.domain.spell.model.enums.CastingUnit;
import club.ttg.dnd5.domain.spell.model.enums.DistanceUnit;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpell;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpellAreaOfEffect;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgCantripScalingTier;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgDamagePart;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpellComponents;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpellScaling;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class VttgSpellMapper {
    private static final List<Integer> CANTRIP_SCALING_LEVELS = List.of(5, 11, 17);
    private static final Pattern DICE_COUNT = Pattern.compile("(?iu)(\\d+)(\\s*[кkd]\\s*\\d+)");
    private static final Set<String> CLASS_KEYS = Set.of(
            "artificer", "barbarian", "bard", "cleric", "druid", "fighter",
            "monk", "paladin", "ranger", "rogue", "sorcerer", "warlock", "wizard"
    );

    private final VttgMarkupConverter markupConverter;
    private final VttgSpellMechanicsExtractor mechanicsExtractor;
    private final VttgSpellScalingExtractor scalingExtractor;

    public VttgSpell toVttg(Spell spell) {
        SpellCastingTime castingTime = primaryCastingTime(spell);
        SpellDistance range = first(spell.getRange());
        SpellDuration duration = first(spell.getDuration());
        String description = markupConverter.toText(spell.getDescription());
        VttgSpellMechanics mechanics = mechanicsExtractor.extract(spell, description);
        SpellEffect effect = spell.getEffect();
        VttgSpellAreaOfEffect areaOfEffect = areaOfEffect(effect == null ? null : effect.getAreaOfEffect());
        String higherLevelDescription = optionalText(spell.getUpper());
        VttgSpellScaling scaling = scalingExtractor.extract(spell.getUpcastable(), higherLevelDescription);
        List<VttgCantripScalingTier> cantripScalingTiers = cantripScalingTiers(spell, mechanics, higherLevelDescription);

        return VttgSpell.builder()
                .id(spell.getUrl())
                .name(spell.getName())
                .nameEn(spell.getEnglish())
                .level(spell.getLevel())
                .school(spell.getSchool().getSchool().name().toLowerCase(Locale.ROOT))
                .castingTimeValue(valueOrDefault(castingTime == null ? null : castingTime.getValue(), 1))
                .castingTimeUnit(castingTimeUnit(castingTime))
                .reactionTrigger(reactionTrigger(castingTime))
                .components(components(spell.getComponents()))
                .range(valueOrDefault(range == null ? null : range.getValue(), 0))
                .rangeUnit(rangeUnit(range))
                .rangeSpecial(rangeSpecial(range))
                .durationValue(valueOrDefault(duration == null ? null : duration.getValue(), 0))
                .durationUnit(durationUnit(duration))
                .concentration(duration != null && Boolean.TRUE.equals(duration.getConcentration()))
                .ritual(isRitual(spell))
                .areaOfEffect(areaOfEffect)
                .targetType(targetType(effect, range, areaOfEffect))
                .targetCount(effect == null ? null : effect.getTargetCount())
                .deliveryType(deliveryType(effect, range))
                .damageParts(mechanics.damageParts())
                .autoHit(effect == null ? null : effect.getAutoHit())
                .saveType(saveType(effect))
                .saveEffect(mechanics.saveEffect())
                .cantripScaling(cantripScalingTiers == null ? null : "level")
                .cantripScalingTiers(cantripScalingTiers)
                .scaling(scaling)
                .description(description)
                .higherLevelDescription(higherLevelDescription)
                .sourceKey(sourceKey(spell.getSource()))
                .isSRD(true)
                .classKeys(classKeys(spell.getClassAffiliation()))
                .type("spell")
                .section("spells")
                .build();
    }

    private SpellCastingTime primaryCastingTime(Spell spell) {
        if (spell.getCastingTime() == null) {
            return null;
        }
        return spell.getCastingTime().stream()
                .filter(Objects::nonNull)
                .filter(time -> time.getUnit() != CastingUnit.RITUAL)
                .findFirst()
                .orElse(null);
    }

    private boolean isRitual(Spell spell) {
        return spell.getCastingTime() != null && spell.getCastingTime().stream()
                .filter(Objects::nonNull)
                .anyMatch(time -> time.getUnit() == CastingUnit.RITUAL);
    }

    private String castingTimeUnit(SpellCastingTime time) {
        if (time == null || time.getUnit() == null) {
            return "action";
        }
        return switch (time.getUnit()) {
            case BONUS -> "bonus";
            case REACTION -> "reaction";
            case MINUTE -> "minute";
            case HOUR -> "hour";
            default -> "action";
        };
    }

    private String reactionTrigger(SpellCastingTime time) {
        return time != null && time.getUnit() == CastingUnit.REACTION
                ? optionalText(time.getCustom())
                : null;
    }

    private VttgSpellComponents components(SpellComponents source) {
        MaterialComponent material = source == null ? null : source.getM();
        return VttgSpellComponents.builder()
                .verbal(source != null && Boolean.TRUE.equals(source.getV()))
                .somatic(source != null && Boolean.TRUE.equals(source.getS()))
                .material(material != null)
                .materialDescription(material == null ? null : optionalText(material.getText()))
                .materialConsumed(material == null ? null : material.getConsumable())
                .build();
    }

    private String rangeUnit(SpellDistance range) {
        if (range == null || range.getUnit() == null) {
            return "ft";
        }
        if (range.getUnit() == DistanceUnit.SELF) {
            return "self";
        }
        return range.getUnit() == DistanceUnit.MILE ? "mi" : "ft";
    }

    private String rangeSpecial(SpellDistance range) {
        if (range == null) {
            return null;
        }
        if (StringUtils.hasText(range.getCustom())) {
            return range.getCustom();
        }
        if (range.getUnit() == null || range.getUnit() == DistanceUnit.FEET || range.getUnit() == DistanceUnit.MILE) {
            return null;
        }
        return range.getUnit().getName();
    }

    private String durationUnit(SpellDuration duration) {
        if (duration == null || duration.getUnit() == null) {
            return "special";
        }
        return switch (duration.getUnit()) {
            case INSTANT -> "instantaneous";
            case ROUND -> "round";
            case MINUTE -> "minute";
            case HOUR -> "hour";
            case DAY -> "day";
            case UNTIL_DISPEL -> "until-dispelled";
            default -> "special";
        };
    }

    private VttgSpellAreaOfEffect areaOfEffect(AreaOfEffect area) {
        if (area == null || area.getType() == null) {
            return null;
        }
        int size = area.getType() == AreaOfEffectType.CYLINDER && area.getValue2() != null
                ? area.getValue2()
                : area.getValue1();
        Integer width = area.getType() == AreaOfEffectType.LINE ? area.getValue2() : null;
        return VttgSpellAreaOfEffect.builder()
                .shape(areaShape(area.getType()))
                .size(size)
                .width(width)
                .unit("ft")
                .build();
    }

    private String areaShape(AreaOfEffectType type) {
        return switch (type) {
            case CONE -> "cone";
            case CUBE -> "rect";
            case LINE -> "ray";
            case CYLINDER, EMANATION, SPHERE -> "circle";
        };
    }

    private String targetType(SpellEffect effect, SpellDistance range, VttgSpellAreaOfEffect areaOfEffect) {
        if (effect != null && effect.getTargetType() != null) {
            return effect.getTargetType().name().toLowerCase(Locale.ROOT);
        }
        if (areaOfEffect != null) {
            return "area";
        }
        if (range != null && range.getUnit() == DistanceUnit.SELF) {
            return "self";
        }
        if (effect != null && (effect.getAttackType() != null || hasValues(effect.getDamageFormulas())
                || hasValues(effect.getHealingTypes()) || hasValues(effect.getSavingThrows()))) {
            return "creature";
        }
        return "none";
    }

    private String deliveryType(SpellEffect effect, SpellDistance range) {
        AttackType attackType = effect == null ? null : effect.getAttackType();
        if (attackType == AttackType.MELEE) {
            return "melee";
        }
        if (attackType == AttackType.RANGE || attackType == AttackType.MELEE_OR_RANGE) {
            return "ranged";
        }
        if (range == null || range.getUnit() == null) {
            return "none";
        }
        return switch (range.getUnit()) {
            case SELF -> "self";
            case TOUCH -> "touch";
            case SIGHT -> "sight";
            default -> "none";
        };
    }

    private String saveType(SpellEffect effect) {
        if (effect == null || !hasValues(effect.getSavingThrows())) {
            return "none";
        }
        Ability ability = effect.getSavingThrows().getFirst();
        return ability.name().toLowerCase(Locale.ROOT);
    }

    private List<VttgCantripScalingTier> cantripScalingTiers(
            Spell spell, VttgSpellMechanics mechanics, String higherLevelDescription) {
        if (!Objects.equals(spell.getLevel(), 0L)
                || !hasValues(mechanics.damageParts())
                || !isCharacterLevelScaling(higherLevelDescription)) {
            return null;
        }

        List<VttgCantripScalingTier> tiers = CANTRIP_SCALING_LEVELS.stream()
                .map(level -> cantripScalingTier(level, mechanics.damageParts()))
                .filter(Objects::nonNull)
                .toList();
        return tiers.isEmpty() ? null : tiers;
    }

    private VttgCantripScalingTier cantripScalingTier(int level, List<VttgDamagePart> baseParts) {
        int multiplier = switch (level) {
            case 5 -> 2;
            case 11 -> 3;
            case 17 -> 4;
            default -> 1;
        };
        List<VttgDamagePart> parts = baseParts.stream()
                .map(part -> scaleDamagePart(part, multiplier))
                .filter(Objects::nonNull)
                .toList();
        return parts.isEmpty()
                ? null
                : VttgCantripScalingTier.builder().level(level).parts(parts).build();
    }

    private VttgDamagePart scaleDamagePart(VttgDamagePart part, int multiplier) {
        String formula = scaleDiceCount(part.getFormula(), multiplier);
        if (!StringUtils.hasText(formula)) {
            return null;
        }
        return VttgDamagePart.builder()
                .formula(formula)
                .target(part.getTarget())
                .type(part.getType())
                .build();
    }

    private String scaleDiceCount(String formula, int multiplier) {
        if (!StringUtils.hasText(formula)) {
            return formula;
        }
        Matcher matcher = DICE_COUNT.matcher(formula);
        return matcher.replaceAll(result -> Integer.parseInt(result.group(1)) * multiplier
                + result.group(2));
    }

    private boolean isCharacterLevelScaling(String higherLevelDescription) {
        if (!StringUtils.hasText(higherLevelDescription)) {
            return false;
        }
        return higherLevelDescription.matches("(?s).*\\b5\\b.*\\b11\\b.*\\b17\\b.*");
    }

    private String sourceKey(Source source) {
        if (source == null) {
            return "srd";
        }
        if ("PHB24".equalsIgnoreCase(source.getAcronym())) {
            return "phb";
        }
        return source.getAcronym().toLowerCase(Locale.ROOT);
    }

    private List<String> classKeys(Set<CharacterClass> classes) {
        if (classes == null) {
            return List.of();
        }
        return classes.stream()
                .filter(Objects::nonNull)
                .map(CharacterClass::getEnglish)
                .filter(StringUtils::hasText)
                .map(value -> value.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", ""))
                .filter(CLASS_KEYS::contains)
                .distinct()
                .sorted()
                .toList();
    }

    private String optionalText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = markupConverter.toText(value);
        return StringUtils.hasText(text) ? text : null;
    }

    private long valueOrDefault(Long value, long fallback) {
        return Optional.ofNullable(value).orElse(fallback);
    }

    private <T> T first(List<T> values) {
        return values == null || values.isEmpty() ? null : values.getFirst();
    }

    private boolean hasValues(List<?> values) {
        return values != null && !values.isEmpty();
    }
}
