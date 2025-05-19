package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.Beast;
import club.ttg.dnd5.domain.beastiary.model.BeastArmor;
import club.ttg.dnd5.domain.beastiary.model.BeastCategory;
import club.ttg.dnd5.domain.beastiary.model.BeastSize;
import club.ttg.dnd5.domain.beastiary.model.BeastSpeeds;
import club.ttg.dnd5.domain.beastiary.model.BeastType;
import club.ttg.dnd5.domain.beastiary.model.ChallengeRating;
import club.ttg.dnd5.domain.beastiary.model.language.BeastLanguages;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastHitResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastShortResponse;
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
        uses = {BeastActionMapper.class, BeastTraitMapper.class, BaseMapping.class})
public interface BeastMapper {
    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = ".", target = "challengeRailing", qualifiedByName = "toChallengeRating")
    BeastShortResponse toShort(Beast beast);

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = ".", target = "header", qualifiedByName = "toHeader")
    @Mapping(source = "armor", target = "armorClass", qualifiedByName = "toArmor")
    @Mapping(source = "initiative", target = "initiative", qualifiedByName = "toInit")
    @Mapping(source = ".", target = "hit", qualifiedByName = "toHit")
    @Mapping(source = "speed", target = "speed", qualifiedByName = "toSpeed")
    @Mapping(source = "languages", target = "languages", qualifiedByName = "toLanguages")
    BeastDetailResponse toDetail(Beast beast);

    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    BeastRequest toRequest(Beast byUrl);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    Beast toEntity(BeastRequest request, Book source);

    @Named("toHeader")
    default String toHeader(Beast beast) {
        var builder = new StringBuilder();
        var type = beast.getCategories()
                .stream()
                .map(BeastCategory::getType)
                .findFirst()
                .orElse(BeastType.HUMANOID);
        builder.append(beast.getSizes()
                .stream()
                .map(BeastSize::getSize)
                .map(size -> size.getSizeName(type))
                .collect(Collectors.joining(" или ")));
        builder.append(beast.getCategories()
                .stream()
                .map(this::getType)
                .collect(Collectors.joining(" или "))
        );
        builder.append(", ");
        builder.append(beast.getAlignment().getName(type));
        return builder.toString();
    }

    @Named("toArmor")
    default String toHeader(BeastArmor beastArmor) {
        var ac = String.valueOf((beastArmor.getArmorClass()));
        if (beastArmor.getText() == null) {
            return ac;
        } else {
            return ac + " " + beastArmor.getText();
        }
    }

    @Named("toInit")
    default String toInit(Byte initiative) {
        String sign = initiative >= 0 ? "+" : "-";
        return String.format("%s%d (%d)", sign, initiative, 10 + initiative);
    }

    @Named("toHit")
    default BeastHitResponse toHit(Beast beast) {
        var response = new BeastHitResponse();
        response.setHit(beast.getHit().getHit());
        response.setFormula(getHitFormula(beast));
        response.setText(beast.getHit().getText());
        return response;
    }

    default String getHitFormula(Beast beast) {
        var builder = new StringBuilder();
        builder.append(beast.getHit().getCountHitDice());
        if (beast.getSizes().size() == 1) {
            builder.append(beast.getSizes().stream()
                    .max(Comparator.comparing(
                            beastSize -> beastSize.getSize().getHitDice().getMaxValue()))
                    .map(BeastSize::getSize)
                    .map(Size::getHitDice)
                    .map(Dice::getName)
                    .orElse("")
            );
        }
        var conMod = beast.getAbilities().getConstitution().getMod();
        if (conMod > 0) {
            builder.append(" + ");
            builder.append(conMod * beast.getHit().getCountHitDice());
        }
        return builder.toString();
    }

    @Named("toSpeed")
    default String toSpeed(BeastSpeeds speeds) {
        return speeds.getText();
    }

    @Named("toLanguages")
    default String toLanguages(BeastLanguages languages) {
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

    default String getType(BeastCategory beastCategory) {
        if (beastCategory.getText() == null) {
            return beastCategory.getType().getName();
        } else {
            return beastCategory.getType().getName().toLowerCase()
                    + " (" + beastCategory.getText() + ")";
        }
    }

    @Named("toChallengeRating")
    default String toChallengeRating(Beast beast) {
        if (beast.getExperience() == null) {
            return String.format("— (Опыт 0; БМ %s)", beast.getExperienceSuffix());
        }
        var lair = beast.getExperienceInLair() == null ? "" : " или " + beast.getExperienceInLair() + " в логове";
        var cr = ChallengeRating.getChallengeRating(beast.getExperience());
        var pb = ChallengeRating.getProficiencyBonus(cr);
        return String.format("%s (Опыт %d%s; БМ %s)", cr, beast.getExperience(), lair, pb);
    }
}
