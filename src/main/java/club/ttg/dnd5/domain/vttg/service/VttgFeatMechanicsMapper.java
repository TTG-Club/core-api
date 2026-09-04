package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.Language;
import club.ttg.dnd5.domain.common.dictionary.SenseType;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.AbilityBonus;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceGrant;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceOption;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceType;
import club.ttg.dnd5.domain.common.model.mechanics.DamageAffinity;
import club.ttg.dnd5.domain.common.model.mechanics.DamageDefenseFromChoice;
import club.ttg.dnd5.domain.common.model.mechanics.DamageDefenseKind;
import club.ttg.dnd5.domain.common.model.mechanics.MechanicChoice;
import club.ttg.dnd5.domain.common.model.mechanics.GrantingMechanics;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.common.model.mechanics.SheetModifiers;
import club.ttg.dnd5.domain.common.model.mechanics.HitPointsModifier;
import club.ttg.dnd5.domain.common.model.mechanics.ProficiencyGrant;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceScaling;
import club.ttg.dnd5.domain.common.model.mechanics.ResourceCounter;
import club.ttg.dnd5.domain.common.model.mechanics.SenseGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SpeedModifier;
import club.ttg.dnd5.domain.common.model.mechanics.SpellFilter;
import club.ttg.dnd5.domain.common.model.mechanics.ClassSpellListGrant;
import club.ttg.dnd5.domain.common.model.mechanics.GrantedSpellRef;
import club.ttg.dnd5.domain.common.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListExpansion;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListGroup;
import club.ttg.dnd5.domain.feat.model.prerequisite.AbilityRequirement;
import club.ttg.dnd5.domain.feat.model.prerequisite.ClassFeatureRequirement;
import club.ttg.dnd5.domain.feat.model.prerequisite.FeatPrerequisite;
import club.ttg.dnd5.domain.item.model.weapon.Mastery;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgEntityRef;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgFeatData;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgFeatMechanics;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgFeatPrerequisite;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Требования и механика черты → формат компендиума VTTG.
 *
 * <p>Работа маппера — перевод словарей: модель источника описывает механику enum'ами
 * ({@code Ability}, {@code Skill}, {@code DamageType}, {@code SenseType}), а потребитель
 * ждёт слаги ({@code strength}, {@code sleightOfHand}, {@code fire}, {@code blindsight}).
 * Переводы живут в {@link VttgDictionaries}, здесь — только раскладка по полям.</p>
 *
 * <p>Пустые блоки опускаются целиком: у черты без механики полей {@code mechanics} и
 * {@code prerequisite} в записи нет вовсе, а не пустые объекты.</p>
 *
 * <p>Одна и та же механика раскладывается в ДВА поля записи, и граница между ними такая:
 * в {@link VttgFeatData} едет всё, что лист персонажа умеет применять сам, в
 * {@link VttgFeatMechanics} — только остаток, которому в типах листа места нет (варианты
 * повышения «или/или», смена типа существа, владения-ссылки без ключа справочника).
 * Дублирования между полями нет: две копии одного и того же разошлись бы при первой же
 * правке, и было бы не понять, какая из них верная.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgFeatMechanicsMapper {

    /** Дискриминант блоба даров у потребителя. */
    private static final String FEAT_DATA_TYPE = "feat";

    /**
     * Виды выбора, которые можно смешать в одном наборе.
     *
     * <p>Ровно те, чей перевод СТРОГИЙ: значение либо нашлось в словаре своего вида, либо
     * нет. На этом и держится смешанный выбор — куда лечь выбранному, потребитель решает
     * по принадлежности самого значения ({@code sleightOfHand} — навык,
     * {@code thieves-tools} — инструмент), и здесь перевод устроен так же.</p>
     *
     * <p>Оружия, приёмов оружия, заклинаний, «варианта» и списка заклинаний здесь нет: их
     * значения описаны данными мира или компендиума, общего справочника у них нет, и
     * непереведённое значение они отдают как есть — в смешанном наборе такой вид забирал
     * бы себе любое значение.</p>
     */
    private static final Set<ChoiceType> MIXABLE_CHOICE_TYPES = EnumSet.of(
            ChoiceType.SKILL, ChoiceType.TOOL, ChoiceType.LANGUAGE, ChoiceType.ABILITY,
            ChoiceType.SAVING_THROW, ChoiceType.SPELLCASTING_ABILITY, ChoiceType.DAMAGE_TYPE,
            ChoiceType.ARMOR);

    /**
     * Нужен одному полю — строке требования: на сайте она размечена так же, как описание
     * ({@code {@class Волшебник|url:wizard-phb}}), и без раскрытия маркеров уехала бы в
     * компендиум фигурными скобками наружу.
     */
    private final VttgMarkupConverter markupConverter;

    /**
     * Названия выдаваемых чертой заклинаний: редактор сохраняет только ссылку, и без
     * справочника в выгрузку уехал бы слаг вместо названия. Запрос идёт только у черт,
     * которые заклинания выдают, — как это устроено и у вида ({@code VttgSpeciesMapper}).
     */
    private final SpellRepository spellRepository;

    /**
     * Черты, на которые ссылаются выбор черты и выдача без выбора: в записи лежит url
     * страницы, а компендиум ищет черту по {@code id}, собранному из английского названия
     * ({@link VttgFeatKeys}). Запрос идёт только у записей, которые черты называют.
     */
    private final FeatRepository featRepository;

    /** Классовые умения-требования в словаре потребителя. */
    private static String classFeature(ClassFeatureRequirement requirement) {
        return switch (requirement) {
            case SPELLCASTING -> "spellcasting";
            case PACT_MAGIC -> "pactMagic";
            case FIGHTING_STYLE -> "fightingStyle";
            case WEAPON_MASTERY -> "weaponMastery";
        };
    }

    /**
     * Что выбирают. Составные имена переводятся в camelCase — как и всё остальное в
     * словарях потребителя.
     */
    private static String choiceType(ChoiceType type) {
        return switch (type) {
            case ABILITY -> "ability";
            case SAVING_THROW -> "savingThrow";
            case SKILL -> "skill";
            case TOOL -> "tool";
            case LANGUAGE -> "language";
            case DAMAGE_TYPE -> "damageType";
            case SPELL -> "spell";
            case CANTRIP -> "cantrip";
            case SPELL_LIST -> "spellList";
            case SPELLCASTING_ABILITY -> "spellcastingAbility";
            case WEAPON -> "weapon";
            case WEAPON_MASTERY -> "weaponMastery";
            case MASTERY_PROPERTY -> "masteryProperty";
            case ARMOR -> "armor";
            case OPTION -> "option";
            case FEAT -> "feat";
        };
    }

    /**
     * Применяемые дары черты ({@code GameItem.featData}) — то, что лист персонажа
     * проставляет сам. Всё, что в эту форму не укладывается, остаётся в
     * {@link #mechanics(Feat)}; см. {@link VttgFeatData}.
     *
     * @return блок даров или {@code null}, если применять нечего
     */
    public VttgFeatData featData(Feat feat) {
        return featData(feat.getMechanics(), prerequisite(feat));
    }

    /**
     * Те же дары, но от механики напрямую — их выдаёт не только черта.
     *
     * <p>Предыстория, вид и умение класса хранят расширенные дары той же моделью и уезжают
     * в компендиум тем же блоком {@code featData}: набор полей у них общий
     * ({@link GrantingMechanics}), и второй маппинг для того же самого разошёлся бы с этим
     * при первой же правке.</p>
     *
     * @param mechanics    дары записи; {@code null} — давать нечего
     * @param prerequisite требования записи; {@code null} — их нет (у всех, кроме черты)
     * @return блок даров или {@code null}, если применять нечего
     */
    public VttgFeatData featData(GrantingMechanics mechanics, VttgFeatPrerequisite prerequisite) {
        ProficiencyGrant grant = mechanics == null ? null : mechanics.getProficiencies();
        SheetModifiers sourceModifiers = mechanics == null ? null : mechanics.getModifiers();

        List<String> skills = grant == null ? List.of() : VttgDictionaries.skills(grant.getSkills());
        List<String> armor = grant == null ? List.of()
                : VttgDictionaries.armorCategories(grant.getArmorCategories());
        List<String> weapons = weaponProficiencies(grant);
        List<String> savingThrows = grant == null ? List.of()
                : VttgDictionaries.abilities(grant.getSavingThrows());

        SpellGrant spellGrant = mechanics == null ? null : mechanics.getSpells();

        VttgFeatData result = VttgFeatData.builder()
                .type(FEAT_DATA_TYPE)
                .skillProficiencies(emptyToNull(skills))
                .savingThrowProficiencies(emptyToNull(savingThrows))
                .armorProficiencies(emptyToNull(armor))
                .weaponProficiencies(emptyToNull(weapons))
                .weaponMasteries(grant == null ? null
                        : emptyToNull(weaponKeys(grant.getWeaponMasteries())))
                .masteryProperties(grant == null ? null
                        : emptyToNull(masteryKeys(grant.getMasteryProperties())))
                .toolProficiencies(grant == null ? null : emptyToNull(toolKeys(grant.getTools())))
                .languages(grant == null ? null
                        : emptyToNull(VttgDictionaries.languages(grant.getLanguages())))
                .damageDefenses(damageDefenses(sourceModifiers))
                .damageDefenseChoices(damageDefenseChoices(sourceModifiers))
                .conditionImmunities(sourceModifiers == null ? null
                        : emptyToNull(VttgDictionaries.conditions(sourceModifiers.getConditionImmunities())))
                .darkvision(darkvision(sourceModifiers))
                .abilityScoreIncrease(abilityScoreIncrease(mechanics))
                .modifiers(featModifiers(sourceModifiers))
                .choices(choices(mechanics == null ? null : mechanics.getChoices()))
                .prerequisite(prerequisite)
                .grantedSpells(grantedSpells(spellGrant))
                .grantedClassSpells(grantedClassSpells(spellGrant))
                .spellcastingAbility(spellGrant == null ? null
                        : VttgDictionaries.ability(spellGrant.getSpellcastingAbility()))
                .grantedSpellsAlwaysPrepared(spellGrant == null ? null
                        : flag(spellGrant.getAlwaysPrepared()))
                .spellList(spellList(mechanics == null ? null : mechanics.getSpellList()))
                .counters(mechanics == null ? null : counters(mechanics.getCounters()))
                .grantedFeats(grantedFeats(mechanics == null ? null : mechanics.getFeats()))
                .build();

        return isEmpty(result) ? null : result;
    }

    /**
     * Черты, выданные без выбора, — ключами компендиума и с названием из справочника.
     *
     * <p>Ссылка на черту, которой в справочнике нет, пропускается: потребитель нашёл бы по
     * ней пустоту, а записать на лист черту без описания и даров — хуже, чем не записать.</p>
     */
    private List<VttgFeatData.GrantedFeat> grantedFeats(List<EntityRef> feats) {
        if (CollectionUtils.isEmpty(feats)) {
            return null;
        }
        List<String> urls = feats.stream()
                .filter(Objects::nonNull)
                .map(ref -> trimmed(ref.getUrl()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<String, Feat> featsByUrl = featsByUrl(urls);
        List<VttgFeatData.GrantedFeat> result = new ArrayList<>();
        for (String url : urls) {
            Feat feat = featsByUrl.get(url);
            if (feat != null) {
                result.add(new VttgFeatData.GrantedFeat(VttgFeatKeys.featId(feat), feat.getName()));
            }
        }
        return emptyToNull(result);
    }

    /** Записи черт по url — одним запросом на все ссылки записи. */
    private Map<String, Feat> featsByUrl(Collection<String> urls) {
        if (urls.isEmpty()) {
            return Map.of();
        }
        Map<String, Feat> result = new LinkedHashMap<>();
        featRepository.findAllById(urls).forEach(feat -> result.put(feat.getUrl(), feat));
        return result;
    }

    /**
     * Категории черт выбора — подписями записи компендиума ({@link VttgFeatKeys#categoryName}).
     */
    private List<String> featCategories(Collection<FeatCategory> categories) {
        if (CollectionUtils.isEmpty(categories)) {
            return null;
        }
        return emptyToNull(categories.stream()
                .filter(Objects::nonNull)
                .map(VttgFeatKeys::categoryName)
                .distinct()
                .toList());
    }

    /**
     * Владение оружием: сначала категории правил, следом конкретные виды.
     *
     * <p>У потребителя это один список — он принимает и {@code martial}, и
     * {@code longsword}. Вид, чьего ключа у листа нет, сюда не попадает: он остаётся
     * ссылкой в {@code mechanics.proficiencies} (см. {@link #unmappedGrant}).</p>
     */
    private List<String> weaponProficiencies(ProficiencyGrant grant) {
        if (grant == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>(VttgDictionaries.weaponCategories(grant.getWeaponCategories()));
        for (String key : weaponKeys(grant.getWeapons())) {
            if (!result.contains(key)) {
                result.add(key);
            }
        }
        return result;
    }

    /**
     * Ступени количества выбора прогрессией по уровням: ключ — уровень строкой, значение —
     * сколько всего выбрано к этому уровню.
     *
     * <p>Той же формы записи, что прогрессия счётчика: потребителю всё равно, растёт ли по
     * уровням запас зарядов или число выбираемых приёмов.</p>
     *
     * @param scaling ступени количества.
     * @return прогрессия по уровням; {@code null} — ступеней нет.
     */
    private Map<String, Integer> choiceScaling(List<ChoiceScaling> scaling) {
        if (CollectionUtils.isEmpty(scaling)) {
            return null;
        }
        Map<String, Integer> result = new LinkedHashMap<>();
        scaling.stream()
                .filter(step -> step != null && step.getLevel() != null && step.getCount() != null)
                .sorted(Comparator.comparingInt(ChoiceScaling::getLevel))
                .forEach(step -> result.put(String.valueOf(step.getLevel()), step.getCount()));
        return result.isEmpty() ? null : result;
    }

    /**
     * Ресурсы черты со счётчиком.
     *
     * <p>Ресурс без ключа или без формулы максимума пропускается: по ключу потребитель
     * хранит потраченный остаток, а формула без значения считается нулём — счётчик,
     * который всегда пуст, на листе только мешает. Название, если его не задали,
     * подставляется ключом: безымянная плитка ни о чём не говорит.</p>
     */
    private List<VttgFeatData.Counter> counters(List<ResourceCounter> counters) {
        if (CollectionUtils.isEmpty(counters)) {
            return null;
        }
        List<VttgFeatData.Counter> result = new ArrayList<>();
        for (ResourceCounter counter : counters) {
            if (counter == null) {
                continue;
            }
            String key = trimmed(counter.getKey());
            String max = trimmed(counter.getMax());
            if (key == null || max == null) {
                continue;
            }
            String name = trimmed(counter.getName());
            result.add(new VttgFeatData.Counter(key, name == null ? key : name,
                    trimmed(counter.getShortName()), max, counter.resolveMin(),
                    VttgDictionaries.recovery(counter.resolveRecovery())));
        }
        return emptyToNull(result);
    }

    /**
     * Заклинания, которые черта даёт знать без выбора.
     *
     * <p>Название берётся из справочника, как у врождённых заклинаний вида: редактор черты
     * сохраняет только url и снимок имени не пишет, поэтому без запроса в выгрузку уехал бы
     * слаг под видом названия — он же осел бы в данных мира при первой правке черты.
     * Снимок имени, если он всё-таки есть, служит запасным вариантом для заклинания,
     * которого в справочнике уже нет.</p>
     *
     * <p>Ссылка без url пропускается: без неё потребитель заклинание не выдаст, а строка в
     * книге заклинаний появилась бы пустой. Порядок редактора сохраняется.</p>
     */
    private List<VttgFeatData.GrantedSpell> grantedSpells(SpellGrant grant) {
        if (grant == null || CollectionUtils.isEmpty(grant.getSpells())) {
            return null;
        }
        List<GrantedSpellRef> refs = grant.getSpells().stream()
                .filter(Objects::nonNull)
                .filter(ref -> trimmed(ref.getUrl()) != null)
                .toList();
        if (refs.isEmpty()) {
            return null;
        }

        Map<String, String> names = spellNames(refs);
        List<VttgFeatData.GrantedSpell> result = new ArrayList<>(refs.size());
        for (GrantedSpellRef ref : refs) {
            String url = trimmed(ref.getUrl());
            String name = names.get(url);
            if (name == null) {
                name = trimmed(ref.getName());
            }
            result.add(new VttgFeatData.GrantedSpell(name == null ? url : name, url,
                    ref.getRequiredLevel(), VttgDictionaries.ability(ref.getSpellcastingAbility()),
                    ref.getAlwaysPrepared()));
        }
        return result;
    }

    /**
     * Списки классов, которые запись выдаёт целиком, — правилом, а не перечнем.
     *
     * <p>Заклинания здесь не разворачиваются нарочно: в мире свой компендиум, и снимок
     * справочника сайта закрыл бы мастеру его собственные заклинания. Потребитель соберёт
     * список сам, сверив {@code spell.classKeys}.</p>
     *
     * <p>Группа без единого канонического класса опускается: сверять в мире будет нечего, а
     * пустой список в записи читался бы как «выдаёт весь компендиум».</p>
     */
    private List<VttgFeatData.GrantedClassSpells> grantedClassSpells(SpellGrant grant) {
        if (grant == null || CollectionUtils.isEmpty(grant.getClassLists())) {
            return null;
        }

        List<VttgFeatData.GrantedClassSpells> result = new ArrayList<>();
        for (ClassSpellListGrant classList : grant.getClassLists()) {
            if (classList == null) {
                continue;
            }
            List<String> classKeys = classKeys(classList.getClasses());
            if (classKeys.isEmpty()) {
                continue;
            }
            result.add(new VttgFeatData.GrantedClassSpells(classKeys, classList.getLevel(),
                    classList.getMaxLevel(), flag(classList.getMaxLevelFromSlots()),
                    classList.getRequiredLevel(),
                    VttgDictionaries.ability(classList.getSpellcastingAbility()),
                    classList.getAlwaysPrepared()));
        }
        return emptyToNull(result);
    }

    /**
     * Заклинания, добавляемые чертой в список заклинаний класса.
     *
     * <p>Собирается так же, как выдача: название добирается из справочника, ссылка без url
     * пропускается. Круг не отдаётся — лист берёт его из записи компендиума, и снимок
     * разошёлся бы с каталогом при первой же правке заклинания.</p>
     *
     * <p>Пустой список схлопывается в {@code null}: блок без заклинаний не описывает
     * ничего, а в записи мира занял бы место и сбил бы сравнение при обновлении.</p>
     */
    private VttgFeatData.SpellList spellList(SpellListExpansion expansion) {
        if (expansion == null) {
            return null;
        }
        // Прежняя плоская форма читается как один список — приведение живёт в модели,
        // чтобы выгрузка и деталь черты разбирали блок одинаково.
        List<SpellListGroup> source = expansion.resolveGroups();
        if (source.isEmpty()) {
            return null;
        }

        // Названия — одним запросом на всю черту, а не на каждый список: списков у метки
        // до пяти, и запрос на каждый превратил бы выгрузку в N+1.
        List<EntityRef> allRefs = source.stream()
                .map(SpellListGroup::getSpells)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .filter(ref -> trimmed(ref.getUrl()) != null)
                .toList();
        if (allRefs.isEmpty()) {
            return null;
        }
        Map<String, String> names = spellNames(allRefs);

        List<VttgFeatData.SpellListGroup> groups = new ArrayList<>(source.size());
        for (SpellListGroup group : source) {
            List<VttgFeatData.SpellListSpell> spells = spellListSpells(group.getSpells(), names);
            // Список без единого пригодного заклинания выбрасывается: пустая ступень в
            // записи мира читалась бы как «на этом уровне не открывается ничего».
            if (spells.isEmpty()) {
                continue;
            }
            groups.add(new VttgFeatData.SpellListGroup(group.getRequiredLevel(),
                    trimmed(group.getCount()), spells));
        }
        if (groups.isEmpty()) {
            return null;
        }
        return new VttgFeatData.SpellList(groups, flag(expansion.getRequiresSpellcasting()));
    }

    /** Заклинания одного списка: ссылка без url пропускается, порядок редактора сохраняется. */
    private List<VttgFeatData.SpellListSpell> spellListSpells(List<EntityRef> refs,
                                                              Map<String, String> names) {
        if (CollectionUtils.isEmpty(refs)) {
            return List.of();
        }
        List<VttgFeatData.SpellListSpell> spells = new ArrayList<>(refs.size());
        for (EntityRef ref : refs) {
            if (ref == null) {
                continue;
            }
            String url = trimmed(ref.getUrl());
            if (url == null) {
                continue;
            }
            String name = names.get(url);
            if (name == null) {
                name = trimmed(ref.getName());
            }
            spells.add(new VttgFeatData.SpellListSpell(name == null ? url : name, url));
        }
        return spells;
    }

    /** Названия заклинаний по их url — одним запросом на черту, как это делает вид. */
    private Map<String, String> spellNames(List<? extends EntityRef> refs) {
        Set<String> urls = refs.stream()
                .map(ref -> trimmed(ref.getUrl()))
                .collect(Collectors.toSet());

        return spellRepository.findAllShortByUrlIn(urls).stream()
                .filter(spell -> StringUtils.hasText(spell.getUrl()) && StringUtils.hasText(spell.getName()))
                .collect(Collectors.toMap(Spell::getUrl, Spell::getName, (first, second) -> first));
    }

    /**
     * Инструменты ключами справочника листа. Ссылка, которой ключа не нашлось, сюда не
     * попадает — она остаётся в {@code mechanics.proficiencies} (см. {@link #toolGrant}),
     * чтобы владение было хотя бы видно.
     */
    private List<String> toolKeys(Collection<EntityRef> tools) {
        if (CollectionUtils.isEmpty(tools)) {
            return List.of();
        }
        return tools.stream()
                .filter(Objects::nonNull)
                .map(ref -> VttgToolKeys.ofUrl(ref.getUrl()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * Виды оружия ключами справочника листа. Ссылка, которой ключа не нашлось, сюда не
     * попадает — она остаётся в {@code mechanics.proficiencies} (см. {@link #unmappedGrant}),
     * чтобы владение было хотя бы видно.
     */
    private List<String> weaponKeys(Collection<EntityRef> weapons) {
        if (CollectionUtils.isEmpty(weapons)) {
            return List.of();
        }
        return weapons.stream()
                .filter(Objects::nonNull)
                .map(ref -> VttgWeaponKeys.ofUrl(ref.getUrl()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * Оружейные приёмы ключами потребителя. Названия совпадают со справочником с точностью
     * до регистра, поэтому словаря им не заведено.
     */
    private List<String> masteryKeys(Collection<Mastery> masteries) {
        if (CollectionUtils.isEmpty(masteries)) {
            return List.of();
        }
        return masteries.stream()
                .filter(Objects::nonNull)
                .map(VttgFeatMechanicsMapper::masteryKey)
                .distinct()
                .toList();
    }

    /** Ключ приёма у потребителя; {@code null} — приёма нет. */
    private static String masteryKey(Mastery mastery) {
        return mastery == null ? null : mastery.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Защиты от урона в форме листа: источник хранит их тремя наборами, потребитель —
     * плоским списком пар «тип урона + вид защиты». Защита по выбору игрока сюда не идёт:
     * тип урона ещё не выбран, и она едет своим полем — см.
     * {@link #damageDefenseChoices(SheetModifiers)}.
     */
    private List<VttgFeatData.DamageDefense> damageDefenses(SheetModifiers modifiers) {
        DamageAffinity damage = modifiers == null ? null : modifiers.getDamage();
        if (damage == null) {
            return null;
        }
        List<VttgFeatData.DamageDefense> result = new ArrayList<>();
        appendDefenses(result, damage.getResistances(), "resistance");
        appendDefenses(result, damage.getImmunities(), "immunity");
        appendDefenses(result, damage.getVulnerabilities(), "vulnerability");
        return emptyToNull(result);
    }

    private void appendDefenses(List<VttgFeatData.DamageDefense> target,
                                Collection<club.ttg.dnd5.domain.common.dictionary.DamageType> types,
                                String kind) {
        for (String damageType : VttgDictionaries.damageTypes(types)) {
            target.add(new VttgFeatData.DamageDefense(damageType, kind));
        }
    }

    /**
     * Защиты от типов урона, которые называет игрок: пара «ключ выбора + исход». Сам тип
     * сюда не едет — он известен только после ответа на выбор из {@code choices}.
     *
     * <p>Записи, сделанные до появления списка, приходят одним легаси-полем
     * {@code resistanceFromChoiceKey}: оно разворачивается в ту же форму, чтобы
     * потребитель знал один способ читать защиту по выбору, а не два.</p>
     */
    private List<VttgFeatData.DamageDefenseChoice> damageDefenseChoices(SheetModifiers modifiers) {
        DamageAffinity damage = modifiers == null ? null : modifiers.getDamage();
        if (damage == null) {
            return null;
        }
        List<DamageDefenseFromChoice> sources = defenseChoices(damage);
        List<VttgFeatData.DamageDefenseChoice> result = sources.stream()
                .map(source -> new VttgFeatData.DamageDefenseChoice(
                        trimmed(source.getChoiceKey()), defenseKind(source.getKind())))
                .filter(choice -> choice.choiceKey() != null)
                .toList();
        return emptyToNull(result);
    }

    /**
     * Защиты по выбору вместе с легаси-полем: список — источник истины, а пустой список
     * с заполненным {@code resistanceFromChoiceKey} означает запись, сделанную до его
     * появления.
     */
    private List<DamageDefenseFromChoice> defenseChoices(DamageAffinity damage) {
        if (!CollectionUtils.isEmpty(damage.getDefenseChoices())) {
            return damage.getDefenseChoices().stream().filter(Objects::nonNull).toList();
        }
        String legacy = trimmed(damage.getResistanceFromChoiceKey());
        return legacy == null ? List.of()
                : List.of(new DamageDefenseFromChoice(legacy, DamageDefenseKind.RESISTANCE));
    }

    /** Исход защиты в словаре потребителя; вид не задан — по умолчанию сопротивление. */
    private String defenseKind(DamageDefenseKind kind) {
        if (kind == null) {
            return "resistance";
        }
        return switch (kind) {
            case RESISTANCE -> "resistance";
            case IMMUNITY -> "immunity";
            case VULNERABILITY -> "vulnerability";
        };
    }

    /** Тёмное зрение — единственное чувство, доходящее до зрения токена. */
    private Integer darkvision(SheetModifiers modifiers) {
        if (modifiers == null || CollectionUtils.isEmpty(modifiers.getSenses())) {
            return null;
        }
        return modifiers.getSenses().stream()
                .filter(Objects::nonNull)
                .filter(sense -> sense.getType() == SenseType.DARKVISION)
                .map(SenseGrant::getRange)
                .filter(Objects::nonNull)
                .filter(range -> range > 0)
                .max(Integer::compareTo)
                .orElse(null);
    }

    /**
     * Повышение характеристик в форме листа. Заполняется только для одного варианта:
     * «+2 к одной либо +1 к двум» — это два взаимоисключающих варианта, и такой выбор
     * форма листа не выражает. Оба варианта целиком остаются в {@code mechanics}.
     *
     * <p>Выбирать нечего — едет готовой прибавкой ({@code fixed}), и лист ставит её сам:
     * у «Крепкого» +1 Телосложения всегда, и спрашивать тут не о чем. Прибавка на выбор
     * ({@code choice}) листом НЕ применяется, пока он не знает выбранного, — она остаётся
     * подсказкой в сводке даров.</p>
     */
    private VttgFeatData.AbilityScoreIncrease abilityScoreIncrease(GrantingMechanics mechanics) {
        if (mechanics == null || mechanics.getAbilityBonuses() == null
                || mechanics.getAbilityBonuses().size() != 1) {
            return null;
        }
        AbilityBonus bonus = mechanics.getAbilityBonuses().get(0);
        if (bonus == null || bonus.getBonus() == null) {
            return null;
        }

        List<String> abilities = VttgDictionaries.abilities(bonus.getAbilities());
        int count = bonus.resolveCount();
        String fromChoiceKey = trimmed(bonus.getFromChoiceKey());
        if (fromChoiceKey == null && abilities.isEmpty()) {
            fromChoiceKey = savingThrowChoiceKey(mechanics);
        }

        // Характеристик ровно столько, сколько поднимают, — выбора нет, и повышение
        // должно встать само
        if (fromChoiceKey == null && !abilities.isEmpty() && abilities.size() == count) {
            Map<String, Integer> fixed = new LinkedHashMap<>();
            for (String ability : abilities) {
                fixed.put(ability, bonus.getBonus());
            }
            return new VttgFeatData.AbilityScoreIncrease(fixed, null, null, bonus.getUpto());
        }

        return new VttgFeatData.AbilityScoreIncrease(null,
                new VttgFeatData.AbilityScoreIncrease.Choice(bonus.getBonus(), count,
                        emptyToNull(abilities)),
                fromChoiceKey,
                bonus.getUpto());
    }

    /**
     * Ключ выбора спасброска, к которому привязывается повышение, когда редактор привязку
     * не проставил.
     *
     * <p>Без {@code fromChoiceKey} повышение по выбору лист не применяет вовсе — оно
     * остаётся подсказкой. А черта, которая уже спросила характеристику, спрашивать её
     * второй раз не должна: так устроен «Устойчивый» — овладел спасбросками Телосложения,
     * его же и поднял. Записи, сохранённые до появления поля, привязки не несут, и без
     * этой подстановки повышение у них молча не работало бы.</p>
     *
     * <p>Условий три, и каждое отсекает догадку. Своего списка характеристик у повышения
     * нет — иначе оно описывает собственный выбор («+1 к Силе или Ловкости»), и привязка
     * подменила бы его чужим ответом. Выбор в черте — спасбросок: {@code ABILITY} по
     * своему смыслу с повышением не связан. И такой выбор один: два подходящих — это
     * неоднозначность, а угадывать здесь хуже, чем не привязать.</p>
     *
     * @return ключ единственного выбора спасброска либо {@code null}
     */
    private String savingThrowChoiceKey(GrantingMechanics mechanics) {
        if (CollectionUtils.isEmpty(mechanics.getChoices())) {
            return null;
        }
        String found = null;
        for (MechanicChoice choice : mechanics.getChoices()) {
            if (choice == null) {
                continue;
            }
            List<ChoiceType> types = choice.resolveTypes();
            if (types.size() != 1 || types.get(0) != ChoiceType.SAVING_THROW) {
                continue;
            }
            String key = trimmed(choice.getKey());
            if (key == null) {
                continue;
            }
            if (found != null) {
                return null;
            }
            found = key;
        }
        return found;
    }

    private boolean isEmpty(VttgFeatData featData) {
        return featData.getSkillProficiencies() == null
                && featData.getSavingThrowProficiencies() == null
                && featData.getArmorProficiencies() == null
                && featData.getWeaponProficiencies() == null
                && featData.getWeaponMasteries() == null
                && featData.getMasteryProperties() == null
                && featData.getToolProficiencies() == null
                && featData.getLanguages() == null
                && featData.getDamageDefenses() == null
                && featData.getDamageDefenseChoices() == null
                && featData.getConditionImmunities() == null
                && featData.getDarkvision() == null
                && featData.getAbilityScoreIncrease() == null
                && featData.getModifiers() == null
                && featData.getChoices() == null
                && featData.getPrerequisite() == null
                && featData.getCounters() == null
                // Заклинательная характеристика и признак подготовки описывают ВЫДАННЫЕ
                // заклинания: без них самих блок даров пуст, и создавать его незачем
                && featData.getGrantedSpells() == null
                && featData.getGrantedClassSpells() == null
                && featData.getSpellList() == null
                && featData.getGrantedFeats() == null;
    }

    /**
     * Разобранное требование черты; {@code null} — требований нет.
     *
     * <p>Черта с одной лишь человекочитаемой строкой требования тоже даёт результат: строка
     * едет полем {@code text}. Иначе требование пропадало бы совсем — в описании его нет,
     * оно лежит отдельной колонкой записи ({@code Feat.prerequisite}).</p>
     */
    public VttgFeatPrerequisite prerequisite(Feat feat) {
        FeatPrerequisite source = feat.getPrerequisiteDetails();
        String text = prerequisiteText(feat, source);
        if (source == null) {
            return text == null ? null : VttgFeatPrerequisite.builder().text(text).build();
        }

        List<VttgFeatPrerequisite.AbilityRequirement> abilities = abilityRequirements(source);
        List<String> classFeatures = source.getClassFeatures() == null ? List.of()
                : source.getClassFeatures().stream()
                        .filter(Objects::nonNull)
                        .map(VttgFeatMechanicsMapper::classFeature)
                        .toList();

        VttgFeatPrerequisite result = VttgFeatPrerequisite.builder()
                .minLevel(source.getMinCharacterLevel())
                .abilityRequirements(emptyToNull(abilities))
                .feats(refs(source.getFeats()))
                .classes(refs(source.getClasses()))
                .species(refs(source.getSpecies()))
                .backgrounds(refs(source.getBackgrounds()))
                .classFeatures(emptyToNull(classFeatures))
                .armorProficiency(emptyToNull(VttgDictionaries.armorCategories(source.getArmorProficiency())))
                .anyDragonmark(Boolean.TRUE.equals(source.getAnyDragonmark()) ? Boolean.TRUE : null)
                .campaign(trimmed(source.getCampaign()))
                .text(text)
                .build();

        // Требование, из которого не перевелось вообще ничего — даже строки, — выводить
        // незачем: пустой объект потребителю ничего не сообщает
        return isEmpty(result) ? null : result;
    }

    /**
     * Требование текстом: сначала то, что редактор отметил как непроверяемое условие, иначе
     * строка требования, как она напечатана в книге.
     *
     * <p>Приоритет у разобранного {@code custom}: его писали именно для листа, а книжная
     * строка перечисляет заодно и то, что уже разобрано в поля, — показывать её поверх
     * значило бы дублировать требования.</p>
     */
    private String prerequisiteText(Feat feat, FeatPrerequisite source) {
        String custom = source == null ? null : trimmed(source.getCustom());
        return custom != null ? custom : trimmed(markupConverter.toText(feat.getPrerequisite()));
    }

    /** Механика черты; {@code null} — механики нет. */
    public VttgFeatMechanics mechanics(Feat feat) {
        FeatMechanics source = feat.getMechanics();
        if (source == null) {
            return null;
        }

        SheetModifiers modifiers = source.getModifiers();
        List<AbilityBonus> variants = source.getAbilityBonuses();

        VttgFeatMechanics result = VttgFeatMechanics.builder()
                // Единственный вариант уже уехал в featData.abilityScoreIncrease — второй
                // копией он разошёлся бы с ним при первой же правке
                .abilityBonuses(variants == null || variants.size() < 2 ? null
                        : abilityBonuses(variants))
                .proficiencies(unmappedGrant(source.getProficiencies()))
                .creatureType(modifiers == null || modifiers.getCreatureType() == null ? null
                        : modifiers.getCreatureType().name().toLowerCase(Locale.ROOT))
                .build();

        return result.getAbilityBonuses() == null && result.getProficiencies() == null
                && result.getCreatureType() == null
                ? null : result;
    }

    // ── Требования ───────────────────────────────────────────────

    private List<VttgFeatPrerequisite.AbilityRequirement> abilityRequirements(FeatPrerequisite source) {
        if (CollectionUtils.isEmpty(source.getAbilities())) {
            return List.of();
        }
        List<VttgFeatPrerequisite.AbilityRequirement> result = new ArrayList<>();
        for (AbilityRequirement requirement : source.getAbilities()) {
            if (requirement == null || requirement.getMinValue() == null) {
                continue;
            }
            List<String> anyOf = VttgDictionaries.abilities(requirement.getAnyOf());
            if (!anyOf.isEmpty()) {
                result.add(new VttgFeatPrerequisite.AbilityRequirement(anyOf, requirement.getMinValue()));
            }
        }
        return result;
    }

    private boolean isEmpty(VttgFeatPrerequisite prerequisite) {
        return prerequisite.getMinLevel() == null
                && prerequisite.getAbilityRequirements() == null
                && prerequisite.getFeats() == null
                && prerequisite.getClasses() == null
                && prerequisite.getSpecies() == null
                && prerequisite.getBackgrounds() == null
                && prerequisite.getClassFeatures() == null
                && prerequisite.getArmorProficiency() == null
                && prerequisite.getAnyDragonmark() == null
                && prerequisite.getCampaign() == null
                && prerequisite.getText() == null;
    }

    // ── Механика ─────────────────────────────────────────────────

    private List<VttgFeatMechanics.AbilityBonus> abilityBonuses(List<AbilityBonus> bonuses) {
        if (CollectionUtils.isEmpty(bonuses)) {
            return null;
        }
        List<VttgFeatMechanics.AbilityBonus> result = new ArrayList<>();
        for (AbilityBonus bonus : bonuses) {
            if (bonus == null) {
                continue;
            }
            List<String> abilities = VttgDictionaries.abilities(bonus.getAbilities());
            result.add(new VttgFeatMechanics.AbilityBonus(emptyToNull(abilities), bonus.getBonus(),
                    bonus.getUpto(), bonus.resolveCount(), trimmed(bonus.getFromChoiceKey())));
        }
        return emptyToNull(result);
    }

    /**
     * Владения, которых нет в справочнике листа: инструменты, виды оружия и приёмы, чьего
     * ключа не нашлось.
     *
     * <p>Те, что нашлись, уехали ключами в {@code featData} и применяются сами; сюда
     * попадает только остаток — ссылкой, чтобы владение было видно и открывалось
     * карточкой. Так одно и то же владение не едет в записи дважды.</p>
     *
     * <p>Навыков, языков, спасбросков и категорий здесь нет: у них словарь закрытый и
     * переводится целиком — остатку взяться неоткуда.</p>
     */
    private VttgFeatMechanics.ProficiencyGrant unmappedGrant(ProficiencyGrant grant) {
        if (grant == null) {
            return null;
        }
        List<VttgEntityRef> tools = refs(unmapped(grant.getTools(), VttgToolKeys::ofUrl));
        List<VttgEntityRef> weapons = refs(unmapped(grant.getWeapons(), VttgWeaponKeys::ofUrl));
        List<VttgEntityRef> masteries = refs(unmapped(grant.getWeaponMasteries(), VttgWeaponKeys::ofUrl));

        return tools == null && weapons == null && masteries == null ? null
                : new VttgFeatMechanics.ProficiencyGrant(tools, weapons, masteries);
    }

    /** Ссылки, которым перевод в ключ справочника листа не нашёлся. */
    private List<EntityRef> unmapped(Collection<EntityRef> refs, Function<String, String> key) {
        if (CollectionUtils.isEmpty(refs)) {
            return List.of();
        }
        return refs.stream()
                .filter(Objects::nonNull)
                .filter(ref -> key.apply(ref.getUrl()) == null)
                .toList();
    }

    /**
     * Модификаторы в форме листа: без защит от урона, иммунитетов к состояниям и
     * тёмного зрения — они едут своими полями {@code featData}, и повторять их здесь
     * значило бы отдать одно и то же дважды.
     */
    private VttgFeatData.Modifiers featModifiers(SheetModifiers source) {
        if (source == null) {
            return null;
        }

        DamageAffinity damage = source.getDamage();

        VttgFeatData.Modifiers result = new VttgFeatData.Modifiers(
                hitPoints(source.getHitPoints()),
                speed(source.getSpeed()),
                source.getArmorClassBonus(),
                senses(source.getSenses()),
                source.getTelepathyRange(),
                damage == null ? null : resistanceFromChoiceKey(damage),
                Boolean.TRUE.equals(source.getInitiativeProficiencyBonus()) ? Boolean.TRUE : null,
                source.getInitiativeBonus());

        return isEmpty(result) ? null : result;
    }

    /**
     * Ключ выбора для легаси-поля {@code modifiers.resistanceFromChoiceKey}: первая защита
     * по выбору, дающая сопротивление. Сборки потребителя, не знающие о
     * {@code damageDefenseChoices}, читают его и получают тот же случай, что и раньше;
     * иммунитет и уязвимость по выбору им остаются не видны — описать их этим полем
     * нечем.
     */
    private String resistanceFromChoiceKey(DamageAffinity damage) {
        return defenseChoices(damage).stream()
                .filter(choice -> choice.getKind() == null
                        || choice.getKind() == DamageDefenseKind.RESISTANCE)
                .map(choice -> trimmed(choice.getChoiceKey()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private boolean isEmpty(VttgFeatData.Modifiers modifiers) {
        return modifiers.hitPoints() == null && modifiers.speed() == null
                && modifiers.armorClassBonus() == null && modifiers.senses() == null
                && modifiers.telepathyRange() == null
                && modifiers.resistanceFromChoiceKey() == null
                && modifiers.initiativeProficiencyBonus() == null
                && modifiers.initiativeBonus() == null;
    }

    private VttgFeatMechanics.HitPoints hitPoints(HitPointsModifier source) {
        if (source == null) {
            return null;
        }
        if (source.getFlat() == null && source.getPerAcquisitionLevel() == null
                && source.getPerLevelAfterAcquisition() == null) {
            return null;
        }
        return new VttgFeatMechanics.HitPoints(source.getFlat(), source.getPerAcquisitionLevel(),
                source.getPerLevelAfterAcquisition());
    }

    private VttgFeatMechanics.Speed speed(SpeedModifier source) {
        if (source == null) {
            return null;
        }
        VttgFeatMechanics.Speed result = new VttgFeatMechanics.Speed(source.getWalkBonus(),
                source.getFly(), source.getClimb(), source.getSwim(),
                flag(source.getFlyEqualsWalk()), flag(source.getClimbEqualsWalk()),
                flag(source.getSwimEqualsWalk()));

        boolean empty = result.walkBonus() == null && result.fly() == null && result.climb() == null
                && result.swim() == null && result.flyEqualsWalk() == null
                && result.climbEqualsWalk() == null && result.swimEqualsWalk() == null;
        return empty ? null : result;
    }

    /**
     * Чувства без тёмного зрения: у потребителя оно живёт зрением токена и приезжает
     * отдельным полем, а не в общем списке.
     */
    private List<VttgFeatMechanics.Sense> senses(List<SenseGrant> senses) {
        if (CollectionUtils.isEmpty(senses)) {
            return null;
        }
        List<VttgFeatMechanics.Sense> result = new ArrayList<>();
        for (SenseGrant sense : senses) {
            if (sense == null || sense.getRange() == null || sense.getRange() <= 0) {
                continue;
            }
            String type = VttgDictionaries.sense(sense.getType());
            if (type != null) {
                result.add(new VttgFeatMechanics.Sense(type, sense.getRange()));
            }
        }
        return emptyToNull(result);
    }

    private List<VttgFeatMechanics.Choice> choices(List<MechanicChoice> choices) {
        if (CollectionUtils.isEmpty(choices)) {
            return null;
        }
        // Черты из выборов черты резолвятся одним запросом на всю запись: у умения их
        // может быть несколько, и запрос на каждый вариант превратил бы выгрузку в N+1.
        // Перечисленные заклинания выборов — тем же приёмом
        Map<String, Feat> featsByUrl = featsByUrl(featOptionUrls(choices));
        Map<String, String> spellNamesByUrl = spellOptionNames(choices);
        List<VttgFeatMechanics.Choice> result = new ArrayList<>();
        for (MechanicChoice choice : choices) {
            if (choice == null) {
                continue;
            }
            List<ChoiceType> types = exportedTypes(choice);
            if (types.isEmpty()) {
                continue;
            }
            List<String> typeKeys = types.stream().map(VttgFeatMechanicsMapper::choiceType).toList();
            result.add(new VttgFeatMechanics.Choice(
                    trimmed(choice.getKey()),
                    typeKeys.get(0),
                    // Один вид описан полем type — списком он повторял бы его без нужды
                    typeKeys.size() > 1 ? typeKeys : null,
                    trimmed(choice.getLabel()),
                    choice.resolveCount(),
                    flag(choice.getCountEqualsProficiencyBonus()),
                    options(types, choice.getOptions(), featsByUrl, spellNamesByUrl),
                    spellFilter(choice.getSpellFilter()),
                    flag(choice.getOnlyIfNotProficient()),
                    flag(choice.getOnlyIfProficient()),
                    flag(choice.getExpertiseIfProficient()),
                    grant(choice.resolveGrant()),
                    flag(choice.getRechooseOnLongRest()),
                    choice.getRequiredLevel(),
                    types.contains(ChoiceType.FEAT) ? featCategories(choice.getFeatCategories()) : null,
                    choiceScaling(choice.getScaling())));
        }
        return emptyToNull(result);
    }

    /** Url черт, перечисленных в выборах черты; у остальных выборов черт нет. */
    private List<String> featOptionUrls(List<MechanicChoice> choices) {
        return choices.stream()
                .filter(Objects::nonNull)
                .filter(choice -> choice.resolveTypes().contains(ChoiceType.FEAT))
                .map(MechanicChoice::getOptions)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .map(option -> trimmed(option.getValue()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * Виды выбора, которые едут в выгрузку.
     *
     * <p>Смешанный набор доезжает целиком, только если смешивать его правда можно
     * ({@link #MIXABLE_CHOICE_TYPES}): потребитель раскладывает выбранное значение по
     * принадлежности, и вид без своего справочника забрал бы себе всё подряд. Набор с
     * таким видом сворачивается до основного — лучше один рабочий вид, чем набор, из
     * которого выбор ложится не туда.</p>
     *
     * @return виды в порядке автора; пустой список — у выбора не задано ни одного
     */
    private List<ChoiceType> exportedTypes(MechanicChoice choice) {
        List<ChoiceType> types = choice.resolveTypes();
        if (types.size() < 2) {
            return types;
        }
        return types.stream().allMatch(MIXABLE_CHOICE_TYPES::contains)
                ? types
                : List.of(types.get(0));
    }

    /**
     * Исход выбора. Владение — значение по умолчанию у потребителя, поэтому в выгрузку
     * идёт только компетентность: иначе поле висело бы у каждого выбора.
     */
    private String grant(ChoiceGrant grant) {
        return grant == ChoiceGrant.EXPERTISE ? "expertise" : null;
    }

    /**
     * Варианты выбора — значениями в словаре ПОТРЕБИТЕЛЯ.
     *
     * <p>Значение выбранного варианта лист кладёт прямо во владения актора, поэтому имя
     * enum'а источника ({@code STEALTH}) до него доезжать не должно: навык с таким ключом
     * молча не проставится, а инструмент со слагом страницы так же молча исчезнет при
     * следующем открытии окна владений. Отсюда перевод по типу выбора.</p>
     */
    private List<VttgFeatMechanics.Option> options(List<ChoiceType> types, List<ChoiceOption> options,
                                                   Map<String, Feat> featsByUrl,
                                                   Map<String, String> spellNamesByUrl) {
        if (CollectionUtils.isEmpty(options)) {
            return null;
        }
        List<VttgFeatMechanics.Option> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ChoiceOption option : options) {
            if (option == null || !StringUtils.hasText(option.getValue())) {
                continue;
            }
            String value = optionValue(types, option.getValue().trim(), featsByUrl);
            if (value == null) {
                // Значение не из словаря типа: у таких выборов у листа есть свой полный
                // справочник, и пустой список вариантов он раскроет целиком — это лучше,
                // чем положить игроку во владения строку, которой в справочнике нет
                continue;
            }
            if (seen.contains(value)) {
                // Разные значения источника сходятся в одно у потребителя: рукопашное и
                // дальнобойное воинское оружие — одна категория правил. Двух одинаковых
                // кнопок в выборе быть не должно
                continue;
            }
            seen.add(value);
            result.add(new VttgFeatMechanics.Option(value,
                    optionName(types, option, featsByUrl, spellNamesByUrl)));
        }
        return emptyToNull(result);
    }

    /**
     * Подпись варианта. У черты и заклинания она берётся из справочника: редактор пишет
     * снимок, но запись могли переименовать, и в выборе игрок должен видеть нынешнее имя.
     * У остальных видов подпись — снимок редактора.
     */
    private String optionName(List<ChoiceType> types, ChoiceOption option, Map<String, Feat> featsByUrl,
                              Map<String, String> spellNamesByUrl) {
        String raw = option.getValue().trim();
        Feat feat = featsByUrl.get(raw);
        if (feat != null && StringUtils.hasText(feat.getName())) {
            return feat.getName();
        }
        boolean isSpell = types.contains(ChoiceType.SPELL) || types.contains(ChoiceType.CANTRIP);
        if (isSpell && StringUtils.hasText(spellNamesByUrl.get(raw))) {
            return spellNamesByUrl.get(raw);
        }
        return trimmed(option.getName());
    }

    /**
     * Названия заклинаний, перечисленных в выборах заклинаний записи, — одним запросом на
     * всю запись. Пусто — перечисленных заклинаний нет, и в справочник ходить незачем.
     */
    private Map<String, String> spellOptionNames(List<MechanicChoice> choices) {
        List<EntityRef> refs = choices.stream()
                .filter(Objects::nonNull)
                .filter(choice -> {
                    List<ChoiceType> types = choice.resolveTypes();
                    return types.contains(ChoiceType.SPELL) || types.contains(ChoiceType.CANTRIP);
                })
                .map(MechanicChoice::getOptions)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .filter(option -> StringUtils.hasText(option.getValue()))
                .map(option -> new EntityRef(option.getValue().trim(), null))
                .toList();
        return refs.isEmpty() ? Map.of() : spellNames(refs);
    }

    /**
     * Значение варианта в словаре потребителя; {@code null} — значение непереводимо и
     * вариант выводить нельзя.
     *
     * <p>Что делать с непереведённым значением, решает не тип ошибки, а наличие у листа
     * своего справочника. Для навыка, характеристики, типа урона, языка и инструмента он
     * есть: выброшенный вариант лист заменит полным списком, а вот чужая строка молча
     * осела бы во владениях актора — инструмент бы из них потом исчез, язык остался бы
     * латинским токеном. Для оружия, «варианта» и заклинаний справочника нет, пул берётся
     * ТОЛЬКО из этого списка — там непереведённое значение сохраняется, иначе выбор
     * остался бы пустым.</p>
     *
     * <p>Заклинание и заговор не переводятся: их значение и так url записи справочника —
     * ровно то, чем потребитель ищет заклинание в компендиуме. Черта переводится в
     * {@code id} записи компендиума; черта, которой в справочнике уже нет, отбрасывается —
     * выбрать её игрок всё равно не смог бы.</p>
     */
    private String optionValue(List<ChoiceType> types, String raw, Map<String, Feat> featsByUrl) {
        for (ChoiceType type : types) {
            String value = optionValue(type, raw, featsByUrl);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String optionValue(ChoiceType type, String raw, Map<String, Feat> featsByUrl) {
        return switch (type) {
            case SKILL -> VttgDictionaries.skill(VttgDictionaries.enumValue(Skill.class, raw));
            case ABILITY, SAVING_THROW, SPELLCASTING_ABILITY ->
                    VttgDictionaries.ability(VttgDictionaries.enumValue(Ability.class, raw));
            case DAMAGE_TYPE -> VttgDictionaries.damageType(VttgDictionaries.damageTypeValue(raw));
            case LANGUAGE -> VttgDictionaries.language(VttgDictionaries.enumValue(Language.class, raw));
            case TOOL -> VttgToolKeys.ofUrl(raw);
            case SPELL_LIST -> firstNonNull(VttgClassKeys.ofUrl(raw), raw);
            case ARMOR -> VttgDictionaries.armorCategory(
                    VttgDictionaries.enumValue(ArmorCategory.class, raw));
            case WEAPON, WEAPON_MASTERY -> firstNonNull(weaponOption(raw), raw);
            // Приёмы у потребителя названы теми же словами, что и в справочнике, только
            // строчными: CLEAVE — cleave, SAP — sap. Своего словаря им не нужно
            case MASTERY_PROPERTY -> masteryKey(VttgDictionaries.enumValue(Mastery.class, raw));
            case SPELL, CANTRIP, OPTION -> raw;
            case FEAT -> {
                Feat feat = featsByUrl.get(raw);
                yield feat == null ? null : VttgFeatKeys.featId(feat);
            }
        };
    }

    private String firstNonNull(String value, String fallback) {
        return value != null ? value : fallback;
    }

    /**
     * Вид оружия в выборе. Редактор задаёт его либо категорией правил, либо ссылкой на
     * конкретное оружие — потребитель принимает и то, и другое, но ссылку ждёт без суффикса
     * источника ({@code longsword}, а не {@code longsword-phb}).
     *
     * <p>Оружейный приём переводится тем же словарём: приём называется по виду оружия,
     * которым владеешь, и значением выбора служит тот же ключ.</p>
     */
    private String weaponOption(String raw) {
        WeaponCategory category = VttgDictionaries.enumValue(WeaponCategory.class, raw);
        if (category != null) {
            return VttgDictionaries.weaponCategory(category);
        }
        return VttgWeaponKeys.ofUrl(raw);
    }

    private VttgFeatMechanics.SpellFilter spellFilter(SpellFilter source) {
        if (source == null) {
            return null;
        }
        List<String> schools = source.getSchools() == null ? List.of()
                : source.getSchools().stream()
                        .filter(Objects::nonNull)
                        .map(school -> school.name().toLowerCase(Locale.ROOT))
                        .toList();
        List<VttgEntityRef> classes = refs(source.getClasses());
        List<String> classKeys = classKeys(source.getClasses());
        String castingTime = source.getCastingTime() == null ? null
                : source.getCastingTime().name().toLowerCase(Locale.ROOT);
        String classesFromChoiceKey = trimmed(source.getClassesFromChoiceKey());

        if (source.getLevel() == null && source.getMaxLevel() == null && schools.isEmpty()
                && classes == null && castingTime == null && classesFromChoiceKey == null) {
            return null;
        }
        return new VttgFeatMechanics.SpellFilter(source.getLevel(), source.getMaxLevel(),
                emptyToNull(schools), classes, emptyToNull(classKeys), classesFromChoiceKey,
                castingTime);
    }

    /**
     * Классы фильтра каноническими ключами — по ним потребитель сверяет
     * {@code spell.classKeys} и собирает пул. Слаг страницы для сверки не годится: он несёт
     * суффикс источника, а ключ заклинания — нет.
     */
    private List<String> classKeys(Collection<EntityRef> classes) {
        if (CollectionUtils.isEmpty(classes)) {
            return List.of();
        }
        return VttgClassKeys.ofUrls(classes.stream()
                .filter(Objects::nonNull)
                .map(EntityRef::getUrl)
                .toList());
    }

    // ── Общее ────────────────────────────────────────────────────

    /** Ссылки на записи справочника; пустой список опускается. */
    private List<VttgEntityRef> refs(Collection<EntityRef> refs) {
        if (CollectionUtils.isEmpty(refs)) {
            return null;
        }
        List<VttgEntityRef> result = new ArrayList<>();
        for (EntityRef ref : refs) {
            if (ref == null || (!StringUtils.hasText(ref.getUrl()) && !StringUtils.hasText(ref.getName()))) {
                continue;
            }
            result.add(new VttgEntityRef(trimmed(ref.getUrl()), trimmed(ref.getName())));
        }
        return emptyToNull(result);
    }

    /** Взведённый флаг; снятый опускается — ложь в выгрузке ничего не сообщает. */
    private Boolean flag(Boolean value) {
        return Boolean.TRUE.equals(value) ? Boolean.TRUE : null;
    }

    private String trimmed(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private <T> List<T> emptyToNull(List<T> values) {
        return CollectionUtils.isEmpty(values) ? null : values;
    }
}
