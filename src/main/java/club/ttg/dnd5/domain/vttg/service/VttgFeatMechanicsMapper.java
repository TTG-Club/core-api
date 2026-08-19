package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Language;
import club.ttg.dnd5.domain.common.dictionary.SenseType;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.AbilityBonus;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceGrant;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceOption;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceType;
import club.ttg.dnd5.domain.common.model.mechanics.DamageAffinity;
import club.ttg.dnd5.domain.common.model.mechanics.MechanicChoice;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.common.model.mechanics.SheetModifiers;
import club.ttg.dnd5.domain.common.model.mechanics.HitPointsModifier;
import club.ttg.dnd5.domain.common.model.mechanics.ProficiencyGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SenseGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SpeedModifier;
import club.ttg.dnd5.domain.common.model.mechanics.SpellFilter;
import club.ttg.dnd5.domain.common.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.feat.model.prerequisite.AbilityRequirement;
import club.ttg.dnd5.domain.feat.model.prerequisite.ClassFeatureRequirement;
import club.ttg.dnd5.domain.feat.model.prerequisite.FeatPrerequisite;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
 */
@Component
@RequiredArgsConstructor
public class VttgFeatMechanicsMapper {

    /** Дискриминант блоба даров у потребителя. */
    private static final String FEAT_DATA_TYPE = "feat";

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
            case OPTION -> "option";
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
        FeatMechanics mechanics = feat.getMechanics();
        VttgFeatPrerequisite prerequisite = prerequisite(feat);
        ProficiencyGrant grant = mechanics == null ? null : mechanics.getProficiencies();
        SheetModifiers sourceModifiers = mechanics == null ? null : mechanics.getModifiers();

        List<String> skills = grant == null ? List.of() : VttgDictionaries.skills(grant.getSkills());
        List<String> armor = grant == null ? List.of()
                : VttgDictionaries.armorCategories(grant.getArmorCategories());
        List<String> weapons = grant == null ? List.of()
                : VttgDictionaries.weaponCategories(grant.getWeaponCategories());

        SpellGrant spellGrant = mechanics == null ? null : mechanics.getSpells();

        VttgFeatData result = VttgFeatData.builder()
                .type(FEAT_DATA_TYPE)
                .skillProficiencies(emptyToNull(skills))
                .armorProficiencies(emptyToNull(armor))
                .weaponProficiencies(emptyToNull(weapons))
                .toolProficiencies(grant == null ? null : emptyToNull(toolKeys(grant.getTools())))
                .languages(grant == null ? null
                        : emptyToNull(VttgDictionaries.languages(grant.getLanguages())))
                .damageDefenses(damageDefenses(sourceModifiers))
                .conditionImmunities(sourceModifiers == null ? null
                        : emptyToNull(VttgDictionaries.conditions(sourceModifiers.getConditionImmunities())))
                .darkvision(darkvision(sourceModifiers))
                .abilityScoreIncrease(abilityScoreIncrease(mechanics))
                .modifiers(featModifiers(sourceModifiers))
                .choices(choices(mechanics == null ? null : mechanics.getChoices()))
                .prerequisite(prerequisite)
                .grantedSpells(grantedSpells(spellGrant))
                .spellcastingAbility(spellGrant == null ? null
                        : VttgDictionaries.ability(spellGrant.getSpellcastingAbility()))
                .grantedSpellsAlwaysPrepared(spellGrant == null ? null
                        : flag(spellGrant.getAlwaysPrepared()))
                .build();

        return isEmpty(result) ? null : result;
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
        List<EntityRef> refs = grant.getSpells().stream()
                .filter(Objects::nonNull)
                .filter(ref -> trimmed(ref.getUrl()) != null)
                .toList();
        if (refs.isEmpty()) {
            return null;
        }

        Map<String, String> names = spellNames(refs);
        List<VttgFeatData.GrantedSpell> result = new ArrayList<>(refs.size());
        for (EntityRef ref : refs) {
            String url = trimmed(ref.getUrl());
            String name = names.get(url);
            if (name == null) {
                name = trimmed(ref.getName());
            }
            result.add(new VttgFeatData.GrantedSpell(name == null ? url : name, url));
        }
        return result;
    }

    /** Названия заклинаний по их url — одним запросом на черту, как это делает вид. */
    private Map<String, String> spellNames(List<EntityRef> refs) {
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
     * Защиты от урона в форме листа: источник хранит их тремя наборами, потребитель —
     * плоским списком пар «тип урона + вид защиты». Сопротивление по выбору
     * ({@code resistanceFromChoiceKey}) сюда не идёт: тип урона ещё не выбран.
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
     */
    private VttgFeatData.AbilityScoreIncrease abilityScoreIncrease(FeatMechanics mechanics) {
        if (mechanics == null || mechanics.getAbilityBonuses() == null
                || mechanics.getAbilityBonuses().size() != 1) {
            return null;
        }
        AbilityBonus bonus = mechanics.getAbilityBonuses().get(0);
        if (bonus == null || bonus.getBonus() == null) {
            return null;
        }
        return new VttgFeatData.AbilityScoreIncrease(
                new VttgFeatData.AbilityScoreIncrease.Choice(bonus.getBonus(), bonus.resolveCount(),
                        emptyToNull(VttgDictionaries.abilities(bonus.getAbilities()))),
                trimmed(bonus.getFromChoiceKey()),
                bonus.getUpto());
    }

    private boolean isEmpty(VttgFeatData featData) {
        return featData.getSkillProficiencies() == null
                && featData.getArmorProficiencies() == null
                && featData.getWeaponProficiencies() == null
                && featData.getToolProficiencies() == null
                && featData.getLanguages() == null
                && featData.getDamageDefenses() == null
                && featData.getConditionImmunities() == null
                && featData.getDarkvision() == null
                && featData.getAbilityScoreIncrease() == null
                && featData.getModifiers() == null
                && featData.getChoices() == null
                && featData.getPrerequisite() == null
                // Заклинательная характеристика и признак подготовки описывают ВЫДАННЫЕ
                // заклинания: без них самих блок даров пуст, и создавать его незачем
                && featData.getGrantedSpells() == null;
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

        VttgFeatMechanics result = VttgFeatMechanics.builder()
                .abilityBonuses(abilityBonuses(source.getAbilityBonuses()))
                .proficiencies(toolGrant(source.getProficiencies()))
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
     * Инструменты, которых нет в справочнике листа. Те, что есть, уехали ключами в
     * {@code featData.toolProficiencies} и применяются сами; сюда попадает только остаток —
     * ссылкой, чтобы владение было видно и открывалось карточкой. Так один и тот же
     * инструмент не едет в записи дважды.
     */
    private VttgFeatMechanics.ProficiencyGrant toolGrant(ProficiencyGrant grant) {
        if (grant == null || CollectionUtils.isEmpty(grant.getTools())) {
            return null;
        }
        List<EntityRef> unmapped = grant.getTools().stream()
                .filter(Objects::nonNull)
                .filter(ref -> VttgToolKeys.ofUrl(ref.getUrl()) == null)
                .toList();
        List<VttgEntityRef> tools = refs(unmapped);

        return tools == null ? null : new VttgFeatMechanics.ProficiencyGrant(tools);
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
                damage == null ? null : trimmed(damage.getResistanceFromChoiceKey()),
                Boolean.TRUE.equals(source.getInitiativeProficiencyBonus()) ? Boolean.TRUE : null,
                source.getInitiativeBonus());

        return isEmpty(result) ? null : result;
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
        List<VttgFeatMechanics.Choice> result = new ArrayList<>();
        for (MechanicChoice choice : choices) {
            if (choice == null || choice.getType() == null) {
                continue;
            }
            result.add(new VttgFeatMechanics.Choice(
                    trimmed(choice.getKey()),
                    choiceType(choice.getType()),
                    trimmed(choice.getLabel()),
                    choice.resolveCount(),
                    flag(choice.getCountEqualsProficiencyBonus()),
                    options(choice.getType(), choice.getOptions()),
                    spellFilter(choice.getSpellFilter()),
                    flag(choice.getOnlyIfNotProficient()),
                    flag(choice.getOnlyIfProficient()),
                    flag(choice.getExpertiseIfProficient()),
                    grant(choice.resolveGrant()),
                    flag(choice.getRechooseOnLongRest())));
        }
        return emptyToNull(result);
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
    private List<VttgFeatMechanics.Option> options(ChoiceType type, List<ChoiceOption> options) {
        if (CollectionUtils.isEmpty(options)) {
            return null;
        }
        List<VttgFeatMechanics.Option> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ChoiceOption option : options) {
            if (option == null || !StringUtils.hasText(option.getValue())) {
                continue;
            }
            String value = optionValue(type, option.getValue().trim());
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
            result.add(new VttgFeatMechanics.Option(value, trimmed(option.getName())));
        }
        return emptyToNull(result);
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
     * ровно то, чем потребитель ищет заклинание в компендиуме.</p>
     */
    private String optionValue(ChoiceType type, String raw) {
        return switch (type) {
            case SKILL -> VttgDictionaries.skill(VttgDictionaries.enumValue(Skill.class, raw));
            case ABILITY, SAVING_THROW, SPELLCASTING_ABILITY ->
                    VttgDictionaries.ability(VttgDictionaries.enumValue(Ability.class, raw));
            case DAMAGE_TYPE -> VttgDictionaries.damageType(VttgDictionaries.damageTypeValue(raw));
            case LANGUAGE -> VttgDictionaries.language(VttgDictionaries.enumValue(Language.class, raw));
            case TOOL -> VttgToolKeys.ofUrl(raw);
            case SPELL_LIST -> firstNonNull(VttgClassKeys.ofUrl(raw), raw);
            case WEAPON -> firstNonNull(weaponOption(raw), raw);
            case SPELL, CANTRIP, OPTION -> raw;
        };
    }

    private String firstNonNull(String value, String fallback) {
        return value != null ? value : fallback;
    }

    /**
     * Вид оружия в выборе. Редактор задаёт его либо категорией правил, либо ссылкой на
     * конкретное оружие — потребитель принимает и то, и другое, но ссылку ждёт без суффикса
     * источника ({@code longsword}, а не {@code longsword-phb}).
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
