package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbility;
import club.ttg.dnd5.domain.beastiary.model.CreatureLair;
import club.ttg.dnd5.domain.beastiary.model.CreatureSkill;
import club.ttg.dnd5.domain.beastiary.model.CreatureSpeeds;
import club.ttg.dnd5.domain.beastiary.model.CreatureTrait;
import club.ttg.dnd5.domain.beastiary.model.action.CreatureAction;
import club.ttg.dnd5.domain.beastiary.model.language.CreatureLanguage;
import club.ttg.dnd5.domain.beastiary.model.speed.FlySpeed;
import club.ttg.dnd5.domain.beastiary.model.speed.Speed;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.dictionary.ChallengeRating;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgCreature;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class VttgCreatureMapper {
    private final VttgMarkupConverter markupConverter;

    public VttgCreature toVttg(Creature creature) {
        return VttgCreature.builder()
                .id(creature.getUrl())
                .entityType("creature")
                .type("creature")
                .autoSaves(true)
                .name(creature.getName())
                .nameEn(creature.getEnglish())
                .description(text(creature.getDescription()))
                .header(header(creature))
                .system(system(creature))
                .source(source(creature))
                .isSRD(true)
                .isReadOnly(true)
                .build();
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
        result.put("speed", speedText(creature.getSpeeds()));
        result.put("movement", movement(creature.getSpeeds()));
        result.put("abilities", abilities(creature));
        result.put("challengeRating", challengeRating);
        result.put("proficiencyBonus", ChallengeRating.getPb(experience));
        result.put("savingThrows", savingThrows(creature));
        result.put("skills", skills(creature.getSkills()));
        result.put("defenses", defenses(creature));
        result.put("senses", senses(creature));
        result.put("languages", languages(creature));
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
                .map(trait -> action(trait.getName(), trait.getDescription()))
                .toList();
    }

    private List<Map<String, Object>> actions(Collection<CreatureAction> actions) {
        if (actions == null) return List.of();
        return actions.stream().filter(Objects::nonNull)
                .map(action -> action(action.getName(), action.getDescription()))
                .toList();
    }

    private Map<String, Object> action(String name, String description) {
        return Map.of(
                "name", value(name),
                "description", paragraphs(description)
        );
    }

    private Map<String, Object> lair(CreatureLair lair) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", value(lair.getName()));
        result.put("description", text(lair.getDescription()));
        result.put("effects", actions(lair.getEffects()));
        result.put("ending", text(lair.getEnding()));
        return result;
    }

    private List<String> paragraphs(String markup) {
        String text = text(markup);
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

    private String speedText(CreatureSpeeds speeds) {
        Map<String, Object> movement = movement(speeds);
        List<String> parts = new ArrayList<>();
        Map.of("walk", "", "climb", "лазая ", "swim", "плавая ", "fly", "летая ", "burrow", "копая ")
                .forEach((key, prefix) -> {
                    int speed = (int) movement.get(key);
                    if (speed > 0) parts.add(prefix + speed + " фт.");
                });
        return String.join(", ", parts);
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

    private String source(Creature creature) {
        Source source = creature.getSource();
        if (source == null) return "SRD " + creature.getSrdVersion();
        return creature.getSourcePage() == null ? source.getAcronym() : source.getAcronym() + ", p. " + creature.getSourcePage();
    }

    private List<String> enumNames(Collection<DamageType> values) {
        return values == null ? List.of() : values.stream().filter(Objects::nonNull)
                .map(value -> value == DamageType.FAIR ? "fire" : value.name().toLowerCase(Locale.ROOT))
                .toList();
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

    private <T> T first(Collection<T> values) {
        return values == null || values.isEmpty() ? null : values.iterator().next();
    }
}
