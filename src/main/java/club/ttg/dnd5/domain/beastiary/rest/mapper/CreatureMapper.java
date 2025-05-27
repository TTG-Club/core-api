package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbilities;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbility;
import club.ttg.dnd5.domain.beastiary.model.CreatureArmor;
import club.ttg.dnd5.domain.beastiary.model.CreatureSize;
import club.ttg.dnd5.domain.beastiary.model.CreatureSkill;
import club.ttg.dnd5.domain.beastiary.model.CreatureSpeeds;
import club.ttg.dnd5.domain.beastiary.model.sense.Senses;
import club.ttg.dnd5.domain.beastiary.model.speed.Speed;
import club.ttg.dnd5.domain.beastiary.rest.dto.AbilitiesResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.AbilityResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureRequest;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.beastiary.model.ChallengeRatingUtil;
import club.ttg.dnd5.domain.beastiary.model.language.CreatureLanguages;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.HitResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureShortResponse;
import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
    uses = {
        ActionMapper.class,
        TraitMapper.class,
        BaseMapping.class
    })
public interface CreatureMapper {
    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = ".", target = "challengeRailing", qualifiedByName = "toShortChallengeRating")
    CreatureShortResponse toShort(Creature creature);

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = ".", target = "header", qualifiedByName = "toHeader")
    @Mapping(source = "armor", target = "armorClass", qualifiedByName = "toArmor")
    @Mapping(source = "initiative", target = "initiative", qualifiedByName = "toInit")
    @Mapping(source = ".", target = "hit", qualifiedByName = "toHit")
    @Mapping(source = ".", target = "abilities", qualifiedByName = "toAbilities")
    @Mapping(source = ".", target = "skills", qualifiedByName = "toSkills")
    @Mapping(source = "speed", target = "speed", qualifiedByName = "toSpeed")
    @Mapping(source = "vulnerabilities", target = "vulnerability", qualifiedByName = "toDamage")
    @Mapping(source = "resistance", target = "resistance", qualifiedByName = "toDamage")
    @Mapping(source = ".", target = "immunity", qualifiedByName = "toImmunity")
    @Mapping(source = "senses", target = "sense", qualifiedByName = "toSense")
    @Mapping(source = "languages", target = "languages", qualifiedByName = "toLanguages")
    @Mapping(source = ".", target = "challengeRailing", qualifiedByName = "toChallengeRating")
    CreatureDetailResponse toDetail(Creature creature);

    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    @Mapping(target = "experience.value", source = "experience")
    @Mapping(target = "experience.inLair", source = "experienceInLair")
    @Mapping(target = "experience.suffix", source = "experienceSuffix")
    CreatureRequest toRequest(Creature creature);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(source = "request.experience.value", target = "experience")
    @Mapping(source = "request.experience.inLair", target = "experienceInLair")
    @Mapping(source = "request.experience.suffix", target = "experienceSuffix")
    @Mapping(target = "source", source = "source")
    Creature toEntity(CreatureRequest request, Book source);

    @Named("toAbilities")
    default AbilitiesResponse toAbilities(Creature creature) {
        var pb = Byte.parseByte(
            ChallengeRatingUtil.getProficiencyBonus(
                    ChallengeRatingUtil.getChallengeRating(creature.getExperience()
        )));
        var response = new AbilitiesResponse();

        response.setStrength(getAbility(creature.getAbilities().getStrength(), pb));
        response.setDexterity(getAbility(creature.getAbilities().getDexterity(), pb));
        response.setConstitution(getAbility(creature.getAbilities().getConstitution(), pb));
        response.setIntelligence(getAbility(creature.getAbilities().getIntelligence(), pb));
        response.setWisdom(getAbility(creature.getAbilities().getWisdom(), pb));
        response.setCharisma(getAbility(creature.getAbilities().getCharisma(), pb));
        return response;
    }

    private AbilityResponse getAbility(CreatureAbility ability, byte pb) {
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
        var type = creature.getCategories()
                .getType()
                .stream()
                .findFirst()
                .orElse(CreatureType.HUMANOID);
        builder.append(creature.getSizes()
                .stream()
                .map(CreatureSize::getSize)
                .map(size -> size.getSizeName(type))
                .collect(Collectors.joining(" или ")));
        builder.append(" ");
        builder.append(creature.getCategories().getType()
                .stream()
                .map(CreatureType::getName)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" или "))
        );
        if (creature.getCategories().getText() != null) {
            builder.append(" (");
            builder.append(creature.getCategories().getText().toLowerCase());
            builder.append(")");
        }
        builder.append(", ");
        builder.append(creature.getAlignment().getName(type));
        return builder.toString();
    }

    @Named("toArmor")
    default String toHeader(CreatureArmor creatureArmor) {
        var ac = String.valueOf((creatureArmor.getArmorClass()));
        if (creatureArmor.getText() == null) {
            return ac;
        } else {
            return ac + " " + creatureArmor.getText();
        }
    }

    @Named("toInit")
    default String toInit(Byte initiative) {
        String sign = initiative >= 0 ? "+" : "-";
        return String.format("%s%d (%d)", sign, initiative, 10 + initiative);
    }

    @Named("toHit")
    default HitResponse toHit(Creature creature) {
        var response = new HitResponse();
        response.setHit(creature.getHit().getHit());
        response.setFormula(getHitFormula(creature));
        response.setText(creature.getHit().getText());
        return response;
    }

    default String getHitFormula(Creature creature) {
        var builder = new StringBuilder();
        builder.append(creature.getHit().getCountHitDice());
        if (creature.getSizes().size() == 1) {
            builder.append(creature.getSizes().stream()
                    .max(Comparator.comparing(
                            creatureSize -> creatureSize.getSize().getHitDice().getMaxValue()))
                    .map(CreatureSize::getSize)
                    .map(Size::getHitDice)
                    .map(Dice::getName)
                    .orElse("")
            );
        }
        var conMod = creature.getAbilities().getConstitution().mod();
        if (conMod > 0) {
            builder.append(" + ");
            builder.append(conMod * creature.getHit().getCountHitDice());
        }
        return builder.toString();
    }

    @Named("toSpeed")
    default String toSpeed(CreatureSpeeds speeds) {
        var builder = new StringBuilder();
        var speedList = new ArrayList<String>(4);
        speedList.add(getSpeedText("", speeds.getWalk()));
        if (!speeds.getFly().isEmpty()) {
            builder.append("летая");
            builder.append(speeds.getFly().stream().map(s ->
                    " %d фт. %s "
                            .formatted(s.getValue(),
                                    StringUtils.hasText(s.getText()) ? "("
                                            + (s.isHover() ? "парит; " : "")
                                            + s.getText() +")" : "")
            ).collect(Collectors.joining(", ")));
            speedList.add(builder.toString());
        }
        if (!speeds.getSwim().isEmpty()) {
            speedList.add(getSpeedText("плавая", speeds.getSwim()));
        }
        if (!speeds.getBurrow().isEmpty()) {
            speedList.add(getSpeedText("копая", speeds.getBurrow()));
        }
        if (!speeds.getClimb().isEmpty()) {
            speedList.add(getSpeedText("лазая", speeds.getClimb()));
        }
        return String.join(", ", speedList);
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
       return creature.getSkills().stream()
               .map(skill -> skill.getSkill().getName() + " +"
                       + getSkillBonus(skill, creature.getAbilities(), creature.getExperience()))
               .collect(Collectors.joining(","));
    }

    private int getSkillBonus(final CreatureSkill skill,
                                final CreatureAbilities abilities,
                                final long experience) {
        return abilities.getMod(skill.getSkill().getAbility())
                + (Integer.parseInt(ChallengeRatingUtil.getProficiencyBonus(ChallengeRatingUtil.getChallengeRating(experience))) * skill.getMultiplier());
    }

    @Named("toDamage")
    default String toDamage(Collection<DamageType> damages) {
        return damages.stream()
                .map(DamageType::getName)
                .map(String::toLowerCase)
                .collect(Collectors.joining(", "));
    }

    @Named("toImmunity")
    default String toImmunity(Creature creature) {
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
        var respons = senses.getSenses().stream()
                .map(sense -> "%s %d фт.".formatted(sense.getType().getName(), sense.getValue())).collect(Collectors.joining(", "));
        if (!respons.isEmpty()) {
            respons+=", ";
        }
        respons+="ПВ " + senses.getPassivePerception();
        return respons;
    }

    @Named("toLanguages")
    default String toLanguages(CreatureLanguages languages) {
        var resonse = languages.getLanguages()
                .stream()
                .map(language -> language.getLanguage().getName() + (StringUtils.hasText(language.getText()) ? language.getText() : ""))
                .collect(Collectors.joining(", "));
        if (languages.getText() != null) {
            resonse += languages.getText();
        }
        if (languages.getTelepathy() != null) {
            resonse += "; " + languages.getTelepathy();
        }
        return resonse;
    }

    @Named("toChallengeRating")
    default String toChallengeRating(Creature creature) {
        if (creature.getExperience() == null) {
            return String.format("— (Опыт 0; БМ %s)", creature.getExperienceSuffix());
        }
        var lair = creature.getExperienceInLair() == null ? "" : " или " + creature.getExperienceInLair() + " в логове";
        var cr = ChallengeRatingUtil.getChallengeRating(creature.getExperience());
        var pb = ChallengeRatingUtil.getProficiencyBonus(cr);
        return String.format("%s (Опыт %d%s; БМ %s)", cr, creature.getExperience(), lair, pb);
    }

    @Named("toShortChallengeRating")
    default String toShortChallengeRating(Creature creature) {
        return ChallengeRatingUtil.getChallengeRating(creature.getExperience());
    }
}
