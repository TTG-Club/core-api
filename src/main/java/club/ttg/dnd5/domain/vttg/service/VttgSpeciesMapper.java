package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceOption;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceType;
import club.ttg.dnd5.domain.common.model.mechanics.DamageAffinity;
import club.ttg.dnd5.domain.common.model.mechanics.MechanicChoice;
import club.ttg.dnd5.domain.common.model.mechanics.ProficiencyGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SheetModifiers;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.model.SpeciesFeature;
import club.ttg.dnd5.domain.species.model.mechanics.SpeciesMechanics;
import club.ttg.dnd5.domain.species.repository.SpeciesInnateSpellView;
import club.ttg.dnd5.domain.species.repository.SpeciesRepository;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesSizeDto;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpecies;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Маппер вида TTG Club в формат компендиума VTTG ({@code type = "species"}).
 *
 * <p>{@code type}/{@code creatureType}/{@code size} берутся из enum'ов и приводятся к нижнему
 * регистру (slug эталона: {@code DRAGON → "dragon"}, {@code MEDIUM → "medium"}); порядок размеров
 * сохраняется как в источнике. {@code key} строится из {@code url} так же, как в
 * {@link VttgBackgroundMapper}.</p>
 *
 * <p>{@code grants} собираются из трёх мест: тёмное зрение — свойство вида
 * ({@code properties.darkVision}), сопротивления и владения навыками — механика самой
 * записи ({@code mechanics}) и механика её умений ({@code features[].mechanics}). Запись,
 * действие которой описано только текстом, даёт пустой {@code grants}.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgSpeciesMapper {
    private static final String TYPE = "species";
    private static final String SECTION = "species";
    private static final String DARKVISION = "darkvision";
    private static final String RESISTANCE = "resistance";
    private static final String SKILL_PROFICIENCY = "skillProficiency";
    /** Ключ синтетического умения врождённых заклинаний; дальше идёт уровень. */
    private static final String INNATE_SPELLS_KEY = "innate-spells";
    private static final String INNATE_SPELLS_NAME = "Врождённые заклинания";

    private final VttgMarkupConverter markupConverter;
    private final SpeciesRepository speciesRepository;
    private final SpellRepository spellRepository;

    public VttgSpecies toVttg(Species species) {
        String key = slug(species.getUrl());
        return VttgSpecies.builder()
                .type(TYPE)
                // id обязателен для раскладки дельты (routeEntity: <id>.json), иначе вид отбрасывается.
                .id(key)
                .section(SECTION)
                .srcSection(SectionType.SPECIES.getValue())
                .srcUrl(species.getUrl())
                .key(key)
                .isSRD(species.getSrdVersion() != null)
                .name(species.getName())
                .nameEn(optional(species.getEnglish()))
                .description(markupConverter.toText(species.getDescription()))
                .sourceKey(VttgSourceKeys.of(species.getSource()))
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

    /**
     * Структурные награды вида: тёмное зрение из свойств, сопротивления и владения
     * навыками — из механики самой записи и её умений.
     *
     * <p>Сопротивления всех источников сводятся в одну награду: у потребителя это единый блок
     * защит, а из какого источника пришёл тип урона, лист не показывает. Сопротивление по
     * выбору игрока ({@code resistanceFromChoiceKey}) сюда не идёт — тип урона ещё не выбран,
     * как и в {@link VttgFeatMechanicsMapper}.</p>
     */
    private List<VttgSpecies.Grant> grants(Species species) {
        List<VttgSpecies.Grant> grants = new ArrayList<>();
        if (species.getDarkVision() != null) {
            grants.add(new VttgSpecies.Grant(DARKVISION, species.getDarkVision(), null, null, null));
        }
        List<String> resistances = VttgDictionaries.damageTypes(resistances(species));
        if (!resistances.isEmpty()) {
            grants.add(new VttgSpecies.Grant(RESISTANCE, null, resistances, null, null));
        }
        grants.addAll(skillGrants(species));
        return grants;
    }

    /** Сопротивления записи и её умений в порядке словаря, без повторов. */
    private Set<DamageType> resistances(Species species) {
        Set<DamageType> result = new TreeSet<>();
        for (SheetModifiers modifiers : mechanics(species).map(SpeciesMechanics::getModifiers)
                .filter(Objects::nonNull).toList()) {
            DamageAffinity damage = modifiers.getDamage();
            if (damage != null && !CollectionUtils.isEmpty(damage.getResistances())) {
                result.addAll(damage.getResistances());
            }
        }
        return result;
    }

    /**
     * Владения навыками: выданные без выбора и выбираемые игроком.
     *
     * <p>Наградами по одной на источник, а не одной общей: у выбора есть своё количество и свой
     * пул («один из Восприятия, Скрытности или Выживания»), и слить два таких выбора в одну
     * запись — значит потерять оба.</p>
     */
    private List<VttgSpecies.Grant> skillGrants(Species species) {
        List<VttgSpecies.Grant> result = new ArrayList<>();
        mechanics(species).forEach(mechanics -> {
            ProficiencyGrant granted = mechanics.getProficiencies();
            if (granted != null && !CollectionUtils.isEmpty(granted.getSkills())) {
                List<String> skills = VttgDictionaries.skills(granted.getSkills());
                // Выбирать не из чего: количество равно списку
                result.add(new VttgSpecies.Grant(SKILL_PROFICIENCY, null, null, skills.size(), skills));
            }
            if (CollectionUtils.isEmpty(mechanics.getChoices())) {
                return;
            }
            for (MechanicChoice choice : mechanics.getChoices()) {
                if (choice == null || choice.getType() != ChoiceType.SKILL) {
                    continue;
                }
                result.add(new VttgSpecies.Grant(SKILL_PROFICIENCY, null, null,
                        choice.resolveCount(), choiceSkills(choice)));
            }
        });
        return result;
    }

    /**
     * Пул выбора. {@code null} — подходит любой навык: у эталона это отсутствующий
     * {@code from}. Значения, которых нет в словаре навыков, отбрасываются — выгрузка не
     * место, чтобы падать на опечатке в одной записи справочника.
     */
    private List<String> choiceSkills(MechanicChoice choice) {
        if (CollectionUtils.isEmpty(choice.getOptions())) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (ChoiceOption option : choice.getOptions()) {
            if (option == null || !StringUtils.hasText(option.getValue())) {
                continue;
            }
            Skill skill = skill(option.getValue().trim());
            if (skill != null) {
                result.add(VttgDictionaries.skill(skill));
            }
        }
        return result.isEmpty() ? null : result;
    }

    private Skill skill(String value) {
        try {
            return Skill.valueOf(value);
        } catch (IllegalArgumentException notASkill) {
            return null;
        }
    }

    /**
     * Механика записи и всех её умений; пустая механика отбрасывается. Своя механика идёт
     * первой: у происхождений умений нет вовсе, и награда приходит только оттуда.
     */
    private Stream<SpeciesMechanics> mechanics(Species species) {
        Stream<SpeciesMechanics> own = Stream.ofNullable(species.getMechanics());
        if (species.getFeatures() == null) {
            return own;
        }
        Stream<SpeciesMechanics> features = species.getFeatures().stream()
                .filter(Objects::nonNull)
                .map(SpeciesFeature::getMechanics)
                .filter(Objects::nonNull);
        return Stream.concat(own, features);
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
        result.addAll(innateSpellFeatures(species));
        return result;
    }

    /**
     * Врождённые заклинания вида — отдельными умениями, по одному на требуемый уровень.
     *
     * <p>В источнике заклинание связано с самим видом, а не с конкретным умением, поэтому
     * привязать его к записи из {@code features} не к чему. Потребителю же выдача
     * заклинаний описывается именно умением с уровнем — так устроены и «Наследие
     * преисподней», и высший эльф. Отсюда синтетическое умение со стабильным ключом
     * {@code innate-spells-<уровень>}.</p>
     *
     * <p>Запрос идёт на каждый вид отдельно. Это осознанно: видов десятки, а результат
     * маппинга кэшируется ({@link VttgPayloadStore}) — заново он считается только для
     * изменившихся записей, а не на каждую выгрузку.</p>
     */
    private List<VttgSpecies.Feature> innateSpellFeatures(Species species) {
        List<SpeciesInnateSpellView> innate = speciesRepository.findInnateSpells(species.getUrl());
        if (CollectionUtils.isEmpty(innate)) {
            return List.of();
        }

        Map<String, String> names = spellNames(innate);
        Map<Integer, List<VttgSpecies.GrantedSpell>> byLevel = new TreeMap<>();

        for (SpeciesInnateSpellView view : innate) {
            String spellUrl = view.getSpellUrl();
            String name = names.get(spellUrl);
            if (!StringUtils.hasText(name)) {
                // Заклинания нет в справочнике — выдавать нечего, и подписать нечем
                continue;
            }
            int level = view.getRequiredLevel() == null ? 1 : Math.max(1, view.getRequiredLevel());
            byLevel.computeIfAbsent(level, key -> new ArrayList<>())
                    .add(new VttgSpecies.GrantedSpell(name, spellUrl));
        }

        List<VttgSpecies.Feature> result = new ArrayList<>(byLevel.size());
        for (Map.Entry<Integer, List<VttgSpecies.GrantedSpell>> entry : byLevel.entrySet()) {
            result.add(new VttgSpecies.Feature(INNATE_SPELLS_KEY + "-" + entry.getKey(),
                    INNATE_SPELLS_NAME, null, null, entry.getKey(), entry.getValue()));
        }
        return result;
    }

    /** Названия заклинаний по их url — одним запросом на вид. */
    private Map<String, String> spellNames(List<SpeciesInnateSpellView> innate) {
        Set<String> urls = innate.stream()
                .map(SpeciesInnateSpellView::getSpellUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (urls.isEmpty()) {
            return Map.of();
        }
        return spellRepository.findAllShortByUrlIn(urls).stream()
                .collect(Collectors.toMap(Spell::getUrl, Spell::getName, (first, second) -> first));
    }

    /**
     * Умение источника. {@code level} отдаётся только если он задан: первый уровень —
     * значение по умолчанию у потребителя, и проставлять его каждому умению незачем.
     *
     * <p>{@code grantedSpells} у умения нет: заклинания вида лежат в связующей таблице и
     * уезжают отдельными умениями ({@link #innateSpellFeatures(Species)}).</p>
     */
    private VttgSpecies.Feature feature(SpeciesFeature feature, List<VttgSpecies.Choice> choices) {
        String key = StringUtils.hasText(feature.getUrl()) ? slug(feature.getUrl()) : slug(feature.getEnglish());
        List<VttgSpecies.Choice> attached = (choices == null || choices.isEmpty()) ? null : choices;
        Integer level = feature.getLevel() != null && feature.getLevel() > 1 ? feature.getLevel() : null;
        return new VttgSpecies.Feature(key, feature.getName(),
                markupConverter.toText(feature.getDescription()), attached, level, null);
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
