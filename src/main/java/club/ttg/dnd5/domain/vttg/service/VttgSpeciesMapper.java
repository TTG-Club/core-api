package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.common.model.mechanics.GrantedSpellRef;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.model.SpeciesFeature;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Маппер вида TTG Club в формат компендиума VTTG ({@code type = "species"}).
 *
 * <p>{@code type}/{@code creatureType}/{@code size} берутся из enum'ов и приводятся к нижнему
 * регистру (slug эталона: {@code DRAGON → "dragon"}, {@code MEDIUM → "medium"}); порядок размеров
 * сохраняется как в источнике. {@code key} строится из {@code url} так же, как в
 * {@link VttgBackgroundMapper}.</p>
 *
 * <p>Дары уезжают блоками {@code featData} — тем же сборщиком, что у черты, предыстории и
 * класса ({@link VttgFeatMechanicsMapper#featData}): {@code featData} записи — механика самой
 * записи, {@code featData} умения — механика этого умения. Тёмное зрение — поле
 * {@code featData.darkvision} своего источника. Запись, действие которой описано только
 * текстом, даров не даёт.</p>
 *
 * <p>Происхождения (дочерние виды) экспортируются самостоятельными записями со ссылкой
 * {@code parentKey} на родителя — комбинирование «вид + происхождение» делает потребитель.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgSpeciesMapper {
    private static final String TYPE = "species";
    private static final String SECTION = "species";
    /** Ключ синтетического умения врождённых заклинаний; дальше идёт уровень. */
    private static final String INNATE_SPELLS_KEY = "innate-spells";
    private static final String INNATE_SPELLS_NAME = "Врождённые заклинания";

    private final VttgMarkupConverter markupConverter;
    private final SpeciesRepository speciesRepository;
    private final SpellRepository spellRepository;
    private final VttgFeatMechanicsMapper mechanicsMapper;

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
                .parentKey(parentKey(species))
                .isSRD(species.getSrdVersion() != null)
                .name(species.getName())
                .nameEn(optional(species.getEnglish()))
                .description(markupConverter.toText(species.getDescription()))
                .sourceKey(VttgSourceKeys.of(species.getSource()))
                .creatureType(creatureType(species.getType()))
                .size(sizes(species.getSizes()))
                .speed(speed(species))
                .featData(mechanicsMapper.featData(species.getMechanics(), null))
                .features(features(species))
                .activeEffects(activeEffects(species))
                .build();
    }

    /** Ключ родительского вида; {@code null} — запись верхнеуровневая. */
    private String parentKey(Species species) {
        return species.getParent() == null ? null : slug(species.getParent().getUrl());
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

    /**
     * Активные эффекты самой записи вида. Отдаются без преобразования — так же, как у
     * черты: мастерская заполняет их сразу в вокабуляре VTTG. Эффекты умений уезжают у
     * своих умений и сюда не сводятся: потребителю важно, какое умение дало эффект.
     */
    private List<ActiveEffect> activeEffects(Species species) {
        return CollectionUtils.isEmpty(species.getActiveEffects()) ? null : species.getActiveEffects();
    }

    /** Скорость пешком всегда присутствует; полёт/лазание/плавание — только при наличии. */
    private VttgSpecies.Speed speed(Species species) {
        return new VttgSpecies.Speed(species.getSpeed(),
                species.getFly(), species.getClimb(), species.getSwim());
    }

    /**
     * Умения вида — как есть, без сворачивания происхождений: происхождения теперь
     * уезжают самостоятельными записями с {@code parentKey}, и их умения лежат в их
     * собственных записях.
     */
    private List<VttgSpecies.Feature> features(Species species) {
        List<VttgSpecies.Feature> result = new ArrayList<>();
        if (species.getFeatures() != null) {
            for (SpeciesFeature feature : species.getFeatures()) {
                if (feature != null) {
                    result.add(feature(feature));
                }
            }
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
                    INNATE_SPELLS_NAME, null, entry.getKey(), entry.getValue(), null, null));
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
     * <p>{@code featData} — дары умения из его механики, тем же сборщиком, что у черты.
     * {@code grantedSpells} берутся у самого умения. Заклинания, сохранённые до того,
     * как они переехали к умению, лежат в связующей таблице и по-прежнему уезжают
     * отдельными умениями ({@link #innateSpellFeatures(Species)}).</p>
     */
    private VttgSpecies.Feature feature(SpeciesFeature feature) {
        String key = StringUtils.hasText(feature.getUrl()) ? slug(feature.getUrl()) : slug(feature.getEnglish());
        Integer level = feature.getLevel() != null && feature.getLevel() > 1 ? feature.getLevel() : null;
        List<ActiveEffect> effects = CollectionUtils.isEmpty(feature.getActiveEffects())
                ? null
                : feature.getActiveEffects();
        return new VttgSpecies.Feature(key, feature.getName(),
                markupConverter.toText(feature.getDescription()), level,
                grantedSpells(feature), effects,
                mechanicsMapper.featData(feature.getMechanics(), null));
    }

    /**
     * Заклинания умения. Имя берётся из справочника: у потребителя запись подписана им, а
     * снимок в ссылке мог устареть — заклинание переименовали уже после сохранения вида.
     */
    private List<VttgSpecies.GrantedSpell> grantedSpells(SpeciesFeature feature) {
        if (CollectionUtils.isEmpty(feature.getGrantedSpells())) {
            return null;
        }

        Set<String> urls = feature.getGrantedSpells().stream()
                .map(GrantedSpellRef::getUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (urls.isEmpty()) {
            return null;
        }

        Map<String, String> names = spellRepository.findAllShortByUrlIn(urls).stream()
                .collect(Collectors.toMap(Spell::getUrl, Spell::getName, (first, second) -> first));

        List<VttgSpecies.GrantedSpell> result = new ArrayList<>(urls.size());
        for (String url : urls) {
            String name = names.get(url);
            if (StringUtils.hasText(name)) {
                // Заклинания нет в справочнике — выдавать нечего, и подписать нечем
                result.add(new VttgSpecies.GrantedSpell(name, url));
            }
        }
        return result.isEmpty() ? null : result;
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
}
