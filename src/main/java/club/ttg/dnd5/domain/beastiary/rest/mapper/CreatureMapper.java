package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbilities;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbility;
import club.ttg.dnd5.domain.beastiary.model.CreatureArmor;
import club.ttg.dnd5.domain.beastiary.model.CreatureCategory;
import club.ttg.dnd5.domain.beastiary.model.CreatureSkill;
import club.ttg.dnd5.domain.beastiary.model.CreatureSpeeds;
import club.ttg.dnd5.domain.beastiary.model.sense.Senses;
import club.ttg.dnd5.domain.beastiary.model.speed.FlySpeed;
import club.ttg.dnd5.domain.beastiary.model.speed.Speed;
import club.ttg.dnd5.domain.beastiary.rest.dto.AbilitiesResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.AbilityResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureRequest;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.ChallengeRating;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.CreatureTreasure;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.beastiary.model.language.CreatureLanguages;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.HitResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureShortResponse;
import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        uses = {
                ActionMapper.class,
                TraitMapper.class,
                BaseMapping.class,
                LegendaryMapper.class
        })
public interface CreatureMapper {
    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = ".", target = "challengeRailing", qualifiedByName = "toShortChallengeRating")
    @Mapping(source = "types", target = "type", qualifiedByName = "toType")
    CreatureShortResponse toShort(Creature creature);

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = ".", target = "header", qualifiedByName = "toHeader")
    @Mapping(source = "armor", target = "armorClass", qualifiedByName = "toArmor")
    @Mapping(source = ".", target = "initiative", qualifiedByName = "toInit")
    @Mapping(source = ".", target = "hit", qualifiedByName = "toHit")
    @Mapping(source = ".", target = "abilities", qualifiedByName = "toAbilities")
    @Mapping(source = ".", target = "skills", qualifiedByName = "toSkills")
    @Mapping(source = "speeds", target = "speed", qualifiedByName = "toSpeed")
    @Mapping(source = ".", target = "vulnerability", qualifiedByName = "toVulnerabilities")
    @Mapping(source = ".", target = "resistance", qualifiedByName = "toResistance")
    @Mapping(source = ".", target = "immunity", qualifiedByName = "toImmunity")
    @Mapping(source = "senses", target = "sense", qualifiedByName = "toSense")
    @Mapping(source = "languages", target = "languages", qualifiedByName = "toLanguages")
    @Mapping(source = ".", target = "challengeRailing", qualifiedByName = "toChallengeRating")
    @Mapping(source = "section.sectionName", target = "section.name.name")
    @Mapping(source = "section.sectionEnglish", target = "section.name.english")
    @Mapping(source = "section.habitats", target = "section.habitats", qualifiedByName = "toHabitats")
    @Mapping(source = "section.treasures", target = "section.treasures", qualifiedByName = "toTreasures")
    @Mapping(source = "section.sectionDescription", target = "section.description")
    @Mapping(source = ".", target = "legendary", qualifiedByName = "toLegendary")
    CreatureDetailResponse toDetail(Creature creature);

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceRequestMapping
    @Mapping(target = "ac.value", source = "armor.armorClass")
    @Mapping(target = "ac.text", source = "armor.text")
    @Mapping(target = "experience.value", source = "experience")
    @Mapping(target = "experience.inLair", source = "experienceInLair")
    @Mapping(target = "experience.suffix", source = "experienceSuffix")
    @Mapping(source = "section.sectionName", target = "section.name.name")
    @Mapping(source = "section.sectionEnglish", target = "section.name.english")
    @Mapping(source = "section.sectionDescription", target = "section.description")
    @Mapping(target = "legendary.actions", source = "legendaryActions")
    @Mapping(target = "legendary.count", source = "legendaryAction")
    @Mapping(target = "legendary.inLair", source = "legendaryActionInLair")

    @Mapping(target = "defenses.vulnerabilities.values", source = "vulnerabilities")
    @Mapping(target = "defenses.vulnerabilities.text", source = "vulnerabilitiesText")
    @Mapping(target = "defenses.resistances.values", source = "resistance")
    @Mapping(target = "defenses.resistances.text", source = "resistanceText")
    @Mapping(target = "defenses.immunities.damage", source = "immunityToDamage")
    @Mapping(target = "defenses.immunities.condition", source = "immunityToCondition")
    @Mapping(target = "defenses.immunities.text", source = "vulnerabilitiesText")
    CreatureRequest toRequest(Creature creature);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.ac.value", target = "armor.armorClass")
    @Mapping(source = "request.ac.text", target = "armor.text")
    @Mapping(source = "request.speeds", target = "speeds")
    @Mapping(source = "request.sizes", target = "sizes")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(source = "request.experience.value", target = "experience")
    @Mapping(source = "request.experience.inLair", target = "experienceInLair")
    @Mapping(source = "request.experience.suffix", target = "experienceSuffix")
    @Mapping(source = "request.section.name.name", target = "section.sectionName")
    @Mapping(source = "request.section.name.english", target = "section.sectionEnglish")
    @Mapping(source = "request.section.subtitle", target = "section.subtitle")
    @Mapping(source = "request.section.habitats", target = "section.habitats")
    @Mapping(source = "request.section.treasures", target = "section.treasures")
    @Mapping(source = "request.section.description", target = "section.sectionDescription")
    @Mapping(source = "request.legendary.actions", target = "legendaryActions")
    @Mapping(source = "request.legendary.count", target = "legendaryAction")
    @Mapping(source = "request.legendary.inLair", target = "legendaryActionInLair")
    @Mapping(source = "request.defenses.vulnerabilities.values", target = "vulnerabilities")
    @Mapping(source = "request.defenses.vulnerabilities.text", target = "vulnerabilitiesText")
    @Mapping(source = "request.defenses.resistances.values", target = "resistance")
    @Mapping(source = "request.defenses.resistances.text", target = "resistanceText")

    @Mapping(source = "request.defenses.immunities.damage", target = "immunityToDamage")
    @Mapping(source = "request.defenses.immunities.condition", target = "immunityToCondition")
    @Mapping(source = "request.defenses.immunities.text", target = "immunityText")
    @Mapping(target = "source", source = "source")
    Creature toEntity(CreatureRequest request, Book source);

    @Named("toAbilities")
    default AbilitiesResponse toAbilities(Creature creature) {
        var pb =  ChallengeRating.getPb(creature.getExperience());
        var response = new AbilitiesResponse();

        response.setStrength(getAbility(creature.getAbilities().getStrength(), pb));
        response.setDexterity(getAbility(creature.getAbilities().getDexterity(), pb));
        response.setConstitution(getAbility(creature.getAbilities().getConstitution(), pb));
        response.setIntelligence(getAbility(creature.getAbilities().getIntelligence(), pb));
        response.setWisdom(getAbility(creature.getAbilities().getWisdom(), pb));
        response.setCharisma(getAbility(creature.getAbilities().getCharisma(), pb));
        return response;
    }

    private AbilityResponse getAbility(CreatureAbility ability, int pb) {
        var response = new AbilityResponse();
        response.setValue(ability.getValue());
        var mod = ability.mod();
        response.setMod(mod >=0 ? "+" + mod : ""+ mod);
        mod += (byte) (ability.getMultiplier() * pb);
        response.setSav(mod >=0 ? "+" + mod : ""+ mod);
        return response;
    }

    @Named("toHeader")
    default String toHeader(Creature creature) {
        var builder = new StringBuilder();
        var type = creature.getTypes()
                .getValues()
                .stream()
                .findFirst()
                .orElse(CreatureType.HUMANOID);
        builder.append(creature.getSizes().getValues().stream()
                .map(size -> size.getSizeName(type))
                .collect(Collectors.joining(" или ")));
        builder.append(" ");
        builder.append(creature.getTypes().getValues()
                .stream()
                .map(CreatureType::getName)
                .map(String::toLowerCase)
                .collect(Collectors.joining(", "))
        );
        if (StringUtils.hasText(creature.getTypes().getText())) {
            builder.append(" (");
            builder.append(creature.getTypes().getText().toLowerCase());
            builder.append(")");
        }
        builder.append(", ");
        builder.append(creature.getAlignment().getName(type).toLowerCase());
        return builder.toString();
    }

    @Named("toType")
    default String toType(CreatureCategory category) {
        return category.getValues().stream()
                .map(CreatureType::getName)
                .collect(Collectors.joining(", "))
                + (StringUtils.hasText(category.getText()) ? " (" + category.getText() + ")" : "");
    }

    @Named("toArmor")
    default String toHeader(CreatureArmor creatureArmor) {
        var ac = String.valueOf((creatureArmor.getArmorClass()));
        if (StringUtils.hasText(creatureArmor.getText())) {
            return ac + " " + creatureArmor.getText();
        } else {
            return ac;
        }
    }

    @Named("toInit")
    default String toInit(Creature creature) {
        var mod = creature.getAbilities().getMod(Ability.DEXTERITY);
        var pb = ChallengeRating.getPb(creature.getExperience());
        var initiative = mod + pb * creature.getInitiative().getMultiplier();
        String sign = initiative >= 0 ? "+" : "";
        return String.format("%s%d (%d)",
                sign, initiative,
                10 + initiative);
    }

    @Named("toHit")
    default HitResponse toHit(Creature creature) {
        var response = new HitResponse();
        if (creature.getHit() != null) {
            if (creature.getHit().getValue() > 0)
            {
                response.setHit(creature.getHit().getValue());
            }
            response.setFormula(getHitFormula(creature));
            response.setText(creature.getHit().getText());
        }
        return response;
    }

    default String getHitFormula(Creature creature) {
        if (creature.getHit().getCountHitDice() != null) {
            var builder = new StringBuilder();
            builder.append(creature.getHit().getCountHitDice());
            builder.append(new LinkedList<>(
                    creature.getSizes().getValues()).getLast().getHitDice().getName());
            var conMod = creature.getAbilities().getConstitution().mod();
            if (conMod > 0) {
                builder.append(" + ");
                builder.append(conMod * creature.getHit().getCountHitDice());
            } else if (conMod < 0) {
                builder.append(" - ");
                builder.append(Math.abs(conMod * creature.getHit().getCountHitDice()));
            }
            return builder.toString();
        }
        return "";
    }

    @Named("toSpeed")
    default String toSpeed(CreatureSpeeds speeds) {
        var builder = new StringBuilder();
        var speedList = new ArrayList<String>(4);
        speedList.add(getSpeedText("", speeds.getWalk()));
        if (!CollectionUtils.isEmpty(speeds.getFly())) {
            builder.append("летая");
            builder.append(speeds.getFly().stream()
                    .map(s ->
                            " %d фт. %s".formatted(s.getValue(), getFly(s))
                    ).collect(Collectors.joining(", ")));
            speedList.add(builder.toString());
        }
        if (!CollectionUtils.isEmpty(speeds.getSwim())) {
            speedList.add(getSpeedText("плавая", speeds.getSwim()));
        }
        if (!CollectionUtils.isEmpty(speeds.getBurrow())) {
            speedList.add(getSpeedText("копая", speeds.getBurrow()));
        }
        if (!CollectionUtils.isEmpty(speeds.getClimb())) {
            speedList.add(getSpeedText("лазая", speeds.getClimb()));
        }
        return String.join(", ", speedList);
    }

    private String getFly(FlySpeed fly) {
        if (fly.isHover() && StringUtils.hasText(fly.getText())) {
            return "(парит; " + fly.getText() + ")";
        }
        if (fly.isHover()) {
            return "(парит)";
        }
        return "";
    }

    private String getSpeedText(final String name,
                                final Collection<Speed> speeds) {
        return name +
                speeds.stream().map(s ->
                        " %d фт.%s"
                                .formatted(s.getValue(),
                                        StringUtils.hasText(s.getText()) ? " (" + s.getText() + ")" : "")
                ).collect(Collectors.joining(", "));
    }

    @Named("toSkills")
    default String toSkills(Creature creature) {
        if (CollectionUtils.isEmpty(creature.getSkills())) {
            return "";
        }
        return creature.getSkills().stream()
                .map(skill -> getSkill(creature, skill))
                .collect(Collectors.joining(", "));
    }

    private String getSkill(Creature creature, CreatureSkill skill) {
        return  skill.getSkill().getName()
                + " +"
                + getSkillBonus(skill, creature.getAbilities(), creature.getExperience())
                + (StringUtils.hasText(skill.getText()) ? " (" + skill.getText() + ")" : "");
    }

    private int getSkillBonus(final CreatureSkill skill,
                              final CreatureAbilities abilities,
                              final long experience) {
        return abilities.getMod(skill.getSkill().getAbility())
                + ChallengeRating.getPb(experience) * skill.getMultiplier()
                + (skill.getBonus() == null ? 0 : skill.getBonus());
    }

    @Named("toVulnerabilities")
    default String toVulnerabilities(Creature creature) {
        if (StringUtils.hasText(creature.getVulnerabilitiesText())) {
            return creature.getVulnerabilitiesText();
        }
        return creature.getVulnerabilities().stream()
                .map(DamageType::getName)
                .map(String::toLowerCase)
                .collect(Collectors.joining(", "));
    }

    @Named("toResistance")
    default String toResistance(Creature creature) {
        if (StringUtils.hasText(creature.getResistanceText())) {
            return creature.getResistanceText();
        }
        return creature.getResistance().stream()
                .map(DamageType::getName)
                .map(String::toLowerCase)
                .collect(Collectors.joining(", "));
    }

    @Named("toImmunity")
    default String toImmunity(Creature creature) {
        if (StringUtils.hasText(creature.getImmunityText())) {
            return creature.getImmunityText();
        }
        if (CollectionUtils.isEmpty(creature.getImmunityToDamage()) && CollectionUtils.isEmpty(creature.getImmunityToCondition())) {
            return null;
        }
        var response = creature.getImmunityToDamage().stream()
                .map(DamageType::getName)
                .map(String::toLowerCase)
                .collect(Collectors.joining(", "));
        if (!CollectionUtils.isEmpty(creature.getImmunityToDamage()) && !CollectionUtils.isEmpty(creature.getImmunityToCondition())) {
            response += "; ";
        }
        response += creature.getImmunityToCondition().stream()
                .map(Condition::getName)
                .map(String::toLowerCase)
                .collect(Collectors.joining(", "));
        return response;
    }

    @Named("toSense")
    default String toSense(Senses senses) {
        var response = new ArrayList<String>();
        if (senses.getBlindsight() != null) {
            response.add("слепое зрение %d фт.".formatted(senses.getBlindsight()));
        }
        if (senses.getDarkvision() != null) {
            var darkvision = "тёмное зрение %d фт.".formatted(senses.getDarkvision());
            if (senses.getUnimpeded() !=null && senses.getUnimpeded()) {
                darkvision += " (магическая тьма не является препятствием)";
            }
            response.add(darkvision);
        }
        if (senses.getTruesight() != null) {
            response.add("истинное зрение %d фт.".formatted(senses.getTruesight()));
        }
        if (senses.getTremorsense() != null) {
            response.add("чувство вибрации %d фт.".formatted(senses.getTremorsense()));
        }
        response.add("пассивная внимательность %d".formatted(senses.getPassivePerception()));
        return String.join(", ", response);
    }

    @Named("toLanguages")
    default String toLanguages(CreatureLanguages languages) {
        var opt = Optional.of(languages);
        if (opt.map(CreatureLanguages::getValues).map(Collection::isEmpty).orElse(true)
                && !StringUtils.hasText(opt.map(CreatureLanguages::getText).orElse(""))
                && !StringUtils.hasText(opt.map(CreatureLanguages::getTelepathy).orElse(""))
        ) {
            return "—";
        }
        var resonse = languages.getValues()
                .stream()
                .map(language -> language.getLanguage().getName() + (StringUtils.hasText(language.getText()) ? language.getText() : ""))
                .collect(Collectors.joining(", "));
        if (StringUtils.hasText(languages.getText())) {
            if (!resonse.isBlank()) {
                resonse += ", ";
            }
            resonse += languages.getText();
        }
        if (StringUtils.hasText(languages.getTelepathy())) {
            if (!resonse.isEmpty()) {
                resonse += "; ";
            }
            resonse += "телепатия %s".formatted(languages.getTelepathy());
        }
        return resonse;
    }

    @Named("toChallengeRating")
    default String toChallengeRating(Creature creature) {
        if (creature.getExperience() == null) {
            return String.format("— (Опыт 0; БМ %s)", creature.getExperienceSuffix());
        }
        var lair = creature.getExperienceInLair() == null ? "" : " или "+ creature.getExperienceInLair() + " в логове";
        var cr = ChallengeRating.getCr(creature.getExperience());
        if (creature.getExperience() < 0) {
            return String.format("%s (Опыт 0; БМ %s)", cr, creature.getExperienceSuffix());
        }
        var pb = ChallengeRating.getPb(creature.getExperience());
        return String.format("%s (Опыт %d%s; БМ +%s)", cr, creature.getExperience(), lair, pb);
    }

    @Named("toShortChallengeRating")
    default String toShortChallengeRating(Creature creature) {
        return ChallengeRating.getCr(creature.getExperience());
    }

    @Named("toHabitats")
    default String toHabitats(Collection<Habitat> habitats) {
        if (CollectionUtils.isEmpty(habitats)) {
            return "";
        }
        return habitats
                .stream()
                .map(Habitat::getName)
                .collect(Collectors.joining(", "));
    }

    @Named("toTreasures")
    default String toTreasures(Collection<CreatureTreasure> treasures) {
        if (CollectionUtils.isEmpty(treasures)) {
            return "";
        }
        return treasures
                .stream()
                .map(CreatureTreasure::getName)
                .collect(Collectors.joining(", "));
    }
}
