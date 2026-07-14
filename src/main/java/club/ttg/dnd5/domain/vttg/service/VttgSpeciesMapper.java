package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.model.SpeciesFeature;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesSizeDto;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpecies;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Маппер вида TTG Club в формат компендиума VTTG ({@code type = "species"}).
 *
 * <p>{@code type}/{@code creatureType}/{@code size} берутся из enum'ов и приводятся к нижнему
 * регистру (slug эталона: {@code DRAGON → "dragon"}, {@code MEDIUM → "medium"}); порядок размеров
 * сохраняется как в источнике. {@code key} строится из {@code url} так же, как в
 * {@link VttgBackgroundMapper}. В {@code grants} структурно доступно только тёмное зрение
 * (см. {@link VttgSpecies}); {@code features} — это {@code SpeciesFeature} без вариантов выбора.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgSpeciesMapper {
    private static final String TYPE = "species";
    private static final String SECTION = "species";
    private static final String DARKVISION = "darkvision";
    /** Запасной ключ источника, если у вида его нет. */
    private static final String SOURCE = "srd";

    private final VttgMarkupConverter markupConverter;

    public VttgSpecies toVttg(Species species) {
        String key = slug(species.getUrl());
        return VttgSpecies.builder()
                .type(TYPE)
                // id обязателен для раскладки дельты (routeEntity: <id>.json), иначе вид отбрасывается.
                .id(key)
                .section(SECTION)
                .key(key)
                .isSRD(true)
                .name(species.getName())
                .nameEn(optional(species.getEnglish()))
                .description(markupConverter.toText(species.getDescription()))
                .sourceKey(sourceKey(species.getSource()))
                .creatureType(creatureType(species.getType()))
                .size(sizes(species.getSizes()))
                .speed(speed(species))
                .grants(grants(species))
                .features(features(species))
                .build();
    }

    private String creatureType(CreatureType type) {
        return type == null ? null : type.name().toLowerCase(Locale.ROOT);
    }

    /** Размеры в порядке источника, кроме {@link Size#UNDEFINED}; пустой список при отсутствии. */
    private List<String> sizes(Collection<SpeciesSizeDto> sizes) {
        if (sizes == null) {
            return List.of();
        }
        return sizes.stream()
                .map(SpeciesSizeDto::getType)
                .filter(Objects::nonNull)
                .filter(size -> size != Size.UNDEFINED)
                .map(size -> size.name().toLowerCase(Locale.ROOT))
                .toList();
    }

    /** Скорость пешком всегда присутствует; полёт/лазание/плавание — только при наличии. */
    private VttgSpecies.Speed speed(Species species) {
        return new VttgSpecies.Speed(species.getSpeed(),
                species.getFly(), species.getClimb(), species.getSwim());
    }

    /** Структурные награды: на данный момент только тёмное зрение (см. {@link VttgSpecies}). */
    private List<VttgSpecies.Grant> grants(Species species) {
        List<VttgSpecies.Grant> grants = new ArrayList<>();
        if (species.getDarkVision() != null) {
            grants.add(new VttgSpecies.Grant(DARKVISION, species.getDarkVision(), null, null, null));
        }
        return grants;
    }

    /**
     * Умения вида. Происхождения (дочерние виды) сворачиваются в {@code choices}
     * «происхожденческого» умения родителя (по маркерам в key/english/name); если такого
     * умения нет, добавляется синтетическое умение «Происхождения» с этими вариантами.
     */
    private List<VttgSpecies.Feature> features(Species species) {
        List<SpeciesFeature> source = species.getFeatures() == null
                ? List.of() : new ArrayList<>(species.getFeatures());
        List<VttgSpecies.Choice> choices = choices(species.getLineages());
        int lineageIndex = choices.isEmpty() ? -1 : lineageFeatureIndex(source);

        List<VttgSpecies.Feature> result = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            result.add(feature(source.get(i), i == lineageIndex ? choices : null));
        }
        if (!choices.isEmpty() && lineageIndex < 0) {
            result.add(new VttgSpecies.Feature("lineage", "Происхождения", null, choices));
        }
        return result;
    }

    private VttgSpecies.Feature feature(SpeciesFeature feature, List<VttgSpecies.Choice> choices) {
        String key = StringUtils.hasText(feature.getUrl()) ? slug(feature.getUrl()) : slug(feature.getEnglish());
        List<VttgSpecies.Choice> attached = (choices == null || choices.isEmpty()) ? null : choices;
        return new VttgSpecies.Feature(key, feature.getName(),
                markupConverter.toText(feature.getDescription()), attached);
    }

    /** Индекс «происхожденческого» умения (lineage/legacy/ancestry/происхожд/наследие) или -1. */
    private int lineageFeatureIndex(List<SpeciesFeature> features) {
        for (int i = 0; i < features.size(); i++) {
            if (isLineageFeature(features.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean isLineageFeature(SpeciesFeature feature) {
        String key = (nullToEmpty(feature.getUrl()) + " " + nullToEmpty(feature.getEnglish()))
                .toLowerCase(Locale.ROOT);
        if (key.contains("lineage") || key.contains("legacy") || key.contains("ancestry")) {
            return true;
        }
        String name = nullToEmpty(feature.getName()).toLowerCase(Locale.ROOT);
        return name.contains("происхожд") || name.contains("наследие");
    }

    /** Видимые происхождения (дочерние виды) как варианты выбора, отсортированные по имени. */
    private List<VttgSpecies.Choice> choices(Collection<Species> lineages) {
        if (lineages == null) {
            return List.of();
        }
        return lineages.stream()
                .filter(Objects::nonNull)
                .filter(lineage -> !lineage.isHiddenEntity())
                .sorted(Comparator.comparing(Species::getName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(this::choice)
                .toList();
    }

    private VttgSpecies.Choice choice(Species lineage) {
        return new VttgSpecies.Choice(slug(lineage.getUrl()), lineage.getName(), choiceDescription(lineage));
    }

    /** Текст варианта: описание происхождения и тексты его собственных умений. */
    private String choiceDescription(Species lineage) {
        StringBuilder builder = new StringBuilder();
        String description = markupConverter.toText(lineage.getDescription());
        if (StringUtils.hasText(description)) {
            builder.append(description);
        }
        if (lineage.getFeatures() != null) {
            for (SpeciesFeature feature : lineage.getFeatures()) {
                String text = markupConverter.toText(feature.getDescription());
                if (!StringUtils.hasText(text)) {
                    continue;
                }
                if (builder.length() > 0) {
                    builder.append("\n\n");
                }
                if (StringUtils.hasText(feature.getName())) {
                    builder.append(feature.getName()).append(": ");
                }
                builder.append(text);
            }
        }
        return builder.toString();
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

    /** kebab-case slug из url: {@code "draconic-flight" → "draconic-flight"}. */
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

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
