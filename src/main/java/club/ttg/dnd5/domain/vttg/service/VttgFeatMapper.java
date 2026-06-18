package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgFeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Маппер черты TTG Club в формат компендиума VTTG ({@code type = "feat"}).
 *
 * <p>Поля {@code type}/{@code featureType}/{@code typeLabel} в эталоне ({@code feats.json})
 * константны для всех черт, повторяемость берётся из {@code repeatability} (null → false).
 * {@code id} строится по английскому имени так же, как ссылка на черту в предысториях
 * (см. {@link VttgBackgroundMapper}): «Two-Weapon Fighting» → {@code srd_feat_two_weapon_fighting}.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgFeatMapper {
    private static final String SOURCE = "srd";
    private static final String TYPE = "feat";
    private static final String TYPE_LABEL = "Черты";

    /**
     * Разделители категорий черт в порядке эталона {@code feats.json}
     * ({@code id}/{@code name} как в VTTG). Категории вне списка получают разделитель
     * с производными значениями (см. {@link #separator(FeatCategory)}).
     */
    private static final Map<FeatCategory, Separator> SEPARATORS = separators();

    private final VttgMarkupConverter markupConverter;

    public VttgFeat toVttg(Feat feat) {
        return VttgFeat.builder()
                .id(featId(feat))
                .name(feat.getName())
                .nameEn(optional(feat.getEnglish()))
                .type(TYPE)
                .source(sourceName(feat.getSource()))
                .sourceKey(sourceKey(feat.getSource()))
                .isSRD(true)
                .featureType(TYPE)
                .repeatable(Boolean.TRUE.equals(feat.getRepeatability()))
                .description(markupConverter.toText(feat.getDescription()))
                .typeLabel(TYPE_LABEL)
                .build();
    }

    /** Порядок категорий для разделителей: известные (как в эталоне) + остальные значения enum. */
    public List<FeatCategory> separatorOrder() {
        List<FeatCategory> order = new ArrayList<>(SEPARATORS.keySet());
        for (FeatCategory category : FeatCategory.values()) {
            if (!order.contains(category)) {
                order.add(category);
            }
        }
        return order;
    }

    /**
     * Запись-разделитель категории для данных компендиума VTTG:
     * {@code {type:"separator", id, name}} (см. {@code CompendiumSeparator}).
     */
    public Map<String, Object> separator(FeatCategory category) {
        Separator separator = SEPARATORS.getOrDefault(category, defaultSeparator(category));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "separator");
        result.put("id", separator.id());
        result.put("name", separator.name());
        return result;
    }

    private static Map<FeatCategory, Separator> separators() {
        Map<FeatCategory, Separator> result = new LinkedHashMap<>();
        result.put(FeatCategory.FIGHTING_STYLE, new Separator("fighting_style", "Боевой стиль"));
        result.put(FeatCategory.GENERAL, new Separator("general_feat", "Общая черта"));
        result.put(FeatCategory.ORIGIN, new Separator("origin_feat", "Черта происхождения"));
        result.put(FeatCategory.EPIC_BOON, new Separator("epic_feat", "Эпическая черта"));
        return result;
    }

    private Separator defaultSeparator(FeatCategory category) {
        if (category == null) {
            return new Separator("other_feat", "Прочие черты");
        }
        return new Separator(category.name().toLowerCase(Locale.ROOT), StringUtils.capitalize(category.getName()));
    }

    /** id черты в схеме эталона: {@code "Two-Weapon Fighting" → "srd_feat_two_weapon_fighting"}. */
    private String featId(Feat feat) {
        String base = StringUtils.hasText(feat.getEnglish()) ? feat.getEnglish() : feat.getUrl();
        return "srd_feat_" + (base == null ? "" : base.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", ""));
    }

    /** Человекочитаемое имя источника для отображения ({@code source.name}); {@code null} опускается. */
    private String sourceName(Source source) {
        return source == null ? null : optional(source.getName());
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

    private String optional(String value) {
        return StringUtils.hasText(value) ? value : null;
    }

    /** Идентификатор и заголовок разделителя категории черт в формате VTTG. */
    private record Separator(String id, String name) {
    }
}
