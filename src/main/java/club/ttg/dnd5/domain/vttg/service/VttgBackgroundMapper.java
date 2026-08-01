package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgBackground;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Маппер предыстории TTG Club в формат компендиума VTTG ({@code type = "background"}).
 *
 * <p>Награды раскладываются по блокам эталона: характеристики → {@code abilityGrant},
 * навыки → {@code skillGrant}, инструменты → {@code toolGrant}, черта → {@code featGrant},
 * снаряжение → {@code equipmentOptions}.</p>
 *
 * <p>Блоки-списки отдаются ВСЕГДА, пустыми при отсутствии данных (см. {@link VttgBackground}):
 * мастер настройки предыстории в VTTG читает их поля напрямую и падает на вырезанном блоке.</p>
 *
 * <p>{@code toolGrant.items} уезжает ЧЕЛОВЕКОЧИТАЕМЫМ текстом, а не идентификаторами —
 * в модели владение инструментами так и хранится ({@code Background.toolProficiency}).
 * Сопоставлять текст с идентификаторами здесь нечем: словарь инструментов живёт в системе
 * D&D на стороне VTTG, там же заводятся недостающие. Ровно так же владение отдаёт и класс
 * ({@code VttgClassMapper}) — обе сущности присылают текст, разбирает его потребитель.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgBackgroundMapper {
    private static final String SOURCE = "srd";
    /** Слаг листа дерева разделов для предысторий (см. {@link VttgCompendiumSections}). */
    private static final String SECTION = "backgrounds";

    private final VttgMarkupConverter markupConverter;
    private final VttgEquipmentMapper equipmentMapper;

    public VttgBackground toVttg(Background background) {
        String id = slug(background.getUrl());
        return VttgBackground.builder()
                .id(id)
                .key(id)
                .name(background.getName())
                .nameEn(optional(background.getEnglish()))
                .description(markupConverter.toText(background.getDescription()))
                .section(SECTION)
                .srcSection(SectionType.BACKGROUND.getValue())
                .srcUrl(background.getUrl())
                .sourceKey(sourceKey(background.getSource()))
                .isSRD(background.getSrdVersion() != null)
                .abilityGrant(abilityGrant(background.getAbilities()))
                .skillGrant(skillGrant(background.getSkillProficiencies()))
                .toolGrant(toolGrant(background.getToolProficiency()))
                .featGrant(featGrant(background.getFeat()))
                .equipmentOptions(equipmentOptions(background))
                .type("background")
                .build();
    }

    /** Характеристики в каноническом порядке (Сила→Харизма), как в эталоне. */
    private VttgBackground.AbilityGrant abilityGrant(Set<Ability> abilities) {
        if (abilities == null || abilities.isEmpty()) {
            return new VttgBackground.AbilityGrant(List.of());
        }
        List<String> values = abilities.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .map(ability -> ability.name().toLowerCase(Locale.ROOT))
                .toList();
        return new VttgBackground.AbilityGrant(values);
    }

    private VttgBackground.SkillGrant skillGrant(Set<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            return new VttgBackground.SkillGrant(List.of());
        }
        List<String> values = skills.stream()
                .filter(Objects::nonNull)
                .map(this::skillSlug)
                .sorted()
                .toList();
        return new VttgBackground.SkillGrant(values);
    }

    /**
     * Владение инструментами — текстом, как хранится в модели (см. javadoc класса).
     * Разметка разбирается: в тексте встречаются ссылки на карточки инструментов.
     */
    private VttgBackground.ToolGrant toolGrant(String toolProficiency) {
        String text = markupConverter.toText(toolProficiency).trim();
        return new VttgBackground.ToolGrant(text.isEmpty() ? List.of() : List.of(text));
    }

    private VttgBackground.FeatGrant featGrant(Feat feat) {
        if (feat == null) {
            return null;
        }
        return new VttgBackground.FeatGrant(featId(feat), feat.getName(), optional(feat.getEnglish()));
    }

    /**
     * Варианты стартового снаряжения. Основной источник — структурированное
     * {@code startingEquipment}: именно его показывает сайт, предметы лежат в нём
     * ссылками и количеством, поэтому в VTTG уезжает разметка со ссылками на карточки,
     * а не проза. Свободный текст {@code equipment} остаётся легаси-запасом для записей,
     * которые на структуру ещё не перевели: у части предысторий он и вовсе без разметки,
     * из-за чего снаряжение доезжало плоским текстом, хотя на сайте было со ссылками.
     */
    private List<VttgBackground.EquipmentOption> equipmentOptions(Background background) {
        List<VttgEquipmentMapper.RenderedOption> rendered =
                equipmentMapper.render(background.getStartingEquipment());
        if (!rendered.isEmpty()) {
            return rendered.stream()
                    .map(option -> new VttgBackground.EquipmentOption(
                            option.description(), option.goldEquivalent()))
                    .toList();
        }

        String equipment = background.getEquipment();
        if (!StringUtils.hasText(equipment)) {
            return List.of();
        }
        return List.of(new VttgBackground.EquipmentOption(markupConverter.toText(equipment), null));
    }

    /** camelCase slug навыка: {@code SLEIGHT_OF_HAND → "sleightOfHand"}. */
    private String skillSlug(Skill skill) {
        String[] parts = skill.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            builder.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        }
        return builder.toString();
    }

    /** id черты в схеме эталона: {@code "Magic Initiate" → "srd_feat_magic_initiate"}. */
    private String featId(Feat feat) {
        String base = StringUtils.hasText(feat.getEnglish()) ? feat.getEnglish() : feat.getUrl();
        return "srd_feat_" + (base == null ? "" : base.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", ""));
    }

    private String slug(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String optional(String value) {
        return StringUtils.hasText(value) ? value : null;
    }

    private String sourceKey(Source source) {
        if (source == null) {
            return SOURCE;
        }
        if ("PHB24".equalsIgnoreCase(source.getAcronym())) {
            return "phb";
        }
        return StringUtils.hasText(source.getAcronym())
                ? source.getAcronym().toLowerCase(Locale.ROOT)
                : SOURCE;
    }
}
