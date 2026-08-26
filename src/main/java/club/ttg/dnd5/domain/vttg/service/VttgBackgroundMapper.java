package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.background.model.BackgroundToolChoice;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgBackground;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Маппер предыстории TTG Club в формат компендиума VTTG ({@code type = "background"}).
 *
 * <p>Награды раскладываются по блокам эталона: характеристики → {@code abilityGrant},
 * навыки → {@code skillGrant}, инструменты → {@code toolGrant}, черта → {@code featGrant},
 * снаряжение → {@code equipmentOptions}. Расширенные дары, которых эти блоки не выражают
 * (языки, защиты, чувства, выборы игрока, выдаваемые заклинания), уезжают блоком
 * {@code featData} — тем же, что у черты; активные эффекты — соседним полем.</p>
 *
 * <p>Блоки-списки отдаются ВСЕГДА, пустыми при отсутствии данных (см. {@link VttgBackground}):
 * мастер настройки предыстории в VTTG читает их поля напрямую и падает на вырезанном блоке.</p>
 *
 * <p>{@code toolGrant.items} уезжает КЛЮЧАМИ вокабуляра стола, когда владение задано
 * ссылками на карточки инструментов, и человекочитаемым текстом у записей, которые на
 * ссылки ещё не перевели ({@code Background.toolProficiency}). Разобрать текст в ключи
 * здесь нечем — сопоставление живёт в справочнике листа на стороне VTTG; ровно так же
 * владение отдаёт и класс ({@code VttgClassMapper}).</p>
 */
@Component
@RequiredArgsConstructor
public class VttgBackgroundMapper {
    /** Слаг листа дерева разделов для предысторий (см. {@link VttgCompendiumSections}). */
    private static final String SECTION = "backgrounds";

    private final VttgMarkupConverter markupConverter;
    private final VttgEquipmentMapper equipmentMapper;
    private final VttgFeatMechanicsMapper mechanicsMapper;
    private final FeatRepository featRepository;

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
                .sourceKey(VttgSourceKeys.of(background.getSource()))
                .isSRD(background.getSrdVersion() != null)
                .abilityGrant(abilityGrant(background.getAbilities()))
                .skillGrant(skillGrant(background.getSkillProficiencies()))
                .toolGrant(toolGrant(background))
                .featGrant(featGrant(background))
                .equipmentOptions(equipmentOptions(background))
                // Требований у предыстории нет: черта требует, предыстория даёт
                .featData(mechanicsMapper.featData(background.getMechanics(), null))
                .activeEffects(activeEffects(background))
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
     * Владение инструментами: ключи вокабуляра стола по ссылкам мастерской, а у записей,
     * которые на ссылки ещё не перевели, — свободный текст, как отдавалось раньше.
     *
     * <p>Ссылка, которой в справочнике листа нет, пропускается: владение, исчезающее при
     * следующем открытии окна владений, хуже отсутствующего (см. {@link VttgToolKeys}).
     * Разметка текста разбирается — в ней встречаются ссылки на карточки инструментов.</p>
     */
    private VttgBackground.ToolGrant toolGrant(Background background) {
        List<String> items = toolKeys(background.getToolProficiencies());
        VttgBackground.ToolChoice choices = toolChoice(background.getToolChoice());

        if (items.isEmpty() && choices == null) {
            String text = markupConverter.toText(background.getToolProficiency()).trim();
            return new VttgBackground.ToolGrant(text.isEmpty() ? List.of() : List.of(text));
        }

        return new VttgBackground.ToolGrant(items, choices);
    }

    /** Владение на выбор игрока; {@code null} — выбора нет либо он ничего не даёт. */
    private VttgBackground.ToolChoice toolChoice(BackgroundToolChoice choice) {
        if (choice == null || choice.getCount() == null || choice.getCount() < 1) {
            return null;
        }
        return new VttgBackground.ToolChoice(choice.getCount(), toolKeys(choice.getFrom()));
    }

    /** Ключи вокабуляра стола по ссылкам на карточки инструментов; неизвестные отброшены. */
    private List<String> toolKeys(List<EntityRef> refs) {
        if (CollectionUtils.isEmpty(refs)) {
            return List.of();
        }
        return refs.stream()
                .filter(Objects::nonNull)
                .map(EntityRef::getUrl)
                .map(VttgToolKeys::ofUrl)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * Активные эффекты предыстории. Отдаются без преобразования: {@link ActiveEffect}
     * заполняется в мастерской сразу в вокабуляре VTTG — так же, как у черты и предмета.
     */
    private List<ActiveEffect> activeEffects(Background background) {
        return CollectionUtils.isEmpty(background.getActiveEffects()) ? null : background.getActiveEffects();
    }

    /**
     * Черта-происхождение вместе с уточнением: «Мудрец» даёт «Посвящённого в магию
     * (Волшебник)» — класс списка заклинаний назван самой предысторией, и без уточнения
     * потребитель спросил бы его у игрока заново.
     */
    private VttgBackground.FeatGrant featGrant(Background background) {
        Feat feat = background.getFeat();
        List<String> choices = featChoiceIds(background.getFeatChoices());

        if (feat == null) {
            // Предыстория без единственной черты, но со списком на выбор: название черты
            // назовёт сам выбор, а блок отдать нужно — иначе выбирать будет не из чего
            return choices.isEmpty() ? null
                    : new VttgBackground.FeatGrant(null, null, null, null, choices);
        }

        return new VttgBackground.FeatGrant(featId(feat), feat.getName(), optional(feat.getEnglish()),
                featSuffix(background.getFeatSuffix()), choices.isEmpty() ? null : choices);
    }

    /**
     * Черты на выбор — идентификаторами схемы эталона.
     *
     * <p>Строятся по записям справочника, а не по адресам ссылок: идентификатор собирается
     * из английского названия ({@code srd_feat_magic_initiate}), а в ссылке лежит слаг
     * страницы с суффиксом источника. Ссылка на удалённую черту пропускается — выбор из
     * несуществующей записи потребитель всё равно не покажет.</p>
     */
    private List<String> featChoiceIds(List<EntityRef> refs) {
        if (CollectionUtils.isEmpty(refs)) {
            return List.of();
        }

        List<String> urls = refs.stream()
                .filter(Objects::nonNull)
                .map(EntityRef::getUrl)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        if (urls.isEmpty()) {
            return List.of();
        }

        Map<String, Feat> featsByUrl = new LinkedHashMap<>();
        featRepository.findAllById(urls).forEach(found -> featsByUrl.put(found.getUrl(), found));

        return urls.stream()
                .map(featsByUrl::get)
                .filter(Objects::nonNull)
                .map(this::featId)
                .distinct()
                .toList();
    }

    /**
     * Уточнение черты без обрамляющих скобок: в модели оно хранится ровно так, как
     * дописывается к названию на странице («(Волшебник)»), а скобки — оформление, а не
     * часть значения.
     */
    private String featSuffix(String suffix) {
        if (!StringUtils.hasText(suffix)) {
            return null;
        }
        String text = suffix.trim();
        if (text.startsWith("(") && text.endsWith(")")) {
            text = text.substring(1, text.length() - 1).trim();
        }
        return optional(text);
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
                            option.description(), option.goldEquivalent(),
                            option.items(), option.coins(), option.coin()))
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

}
