package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbilities;
import club.ttg.dnd5.domain.beastiary.model.CreatureArmor;
import club.ttg.dnd5.domain.beastiary.model.CreatureSize;
import club.ttg.dnd5.domain.beastiary.model.CreatureSkill;
import club.ttg.dnd5.domain.beastiary.model.CreatureSpeeds;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureRequest;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.beastiary.model.ChallengeRatingUtil;
import club.ttg.dnd5.domain.beastiary.model.language.CreatureLanguages;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.HitResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureShortResponse;
import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

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
    @Mapping(source = ".", target = "challengeRailing", qualifiedByName = "toChallengeRating")
    CreatureShortResponse toShort(Creature creature);

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = ".", target = "header", qualifiedByName = "toHeader")
    @Mapping(source = "armor", target = "armorClass", qualifiedByName = "toArmor")
    @Mapping(source = "initiative", target = "initiative", qualifiedByName = "toInit")
    @Mapping(source = ".", target = "hit", qualifiedByName = "toHit")
    @Mapping(source = ".", target = "skills", qualifiedByName = "toSkills")
    @Mapping(source = "speed", target = "speed", qualifiedByName = "toSpeed")
    @Mapping(source = "languages", target = "languages", qualifiedByName = "toLanguages")
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
        builder.append(creature.getCategories().getType()
                .stream()
                .map(CreatureType::getName)
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
        var conMod = creature.getAbilities().getConstitution().getMod();
        if (conMod > 0) {
            builder.append(" + ");
            builder.append(conMod * creature.getHit().getCountHitDice());
        }
        return builder.toString();
    }

    @Named("toSpeed")
    default String toSpeed(CreatureSpeeds speeds) {
        return speeds.getText();
    }

    @Named("toSkills")
    default String toSkills(Creature creature) {
       return creature.getSkills().stream()
               .map(skill -> skill.getSkill().getName() + " + "
                       + getSkillBonus(skill, creature.getAbilities(), creature.getExperience()))
               .collect(Collectors.joining(","));
    }

    private int getSkillBonus(final CreatureSkill skill,
                                final CreatureAbilities abilities,
                                final long experience) {
        return abilities.getMod(skill.getSkill().getAbility())
                + (Integer.parseInt(ChallengeRatingUtil.getProficiencyBonus(ChallengeRatingUtil.getChallengeRating(experience))) * skill.getMultiplier());
    }

    @Named("toLanguages")
    default String toLanguages(CreatureLanguages languages) {
        var resonse = languages.getLanguages()
                .stream()
                .map(language -> language.getLanguage().getName() + language.getText())
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
}
