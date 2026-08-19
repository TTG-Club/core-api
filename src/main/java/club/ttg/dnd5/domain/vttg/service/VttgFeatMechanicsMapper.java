package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.SenseType;
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
import club.ttg.dnd5.domain.feat.model.prerequisite.AbilityRequirement;
import club.ttg.dnd5.domain.feat.model.prerequisite.ClassFeatureRequirement;
import club.ttg.dnd5.domain.feat.model.prerequisite.FeatPrerequisite;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgEntityRef;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgFeatData;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgFeatMechanics;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgFeatPrerequisite;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
public class VttgFeatMechanicsMapper {

    /** Дискриминант блоба даров у потребителя. */
    private static final String FEAT_DATA_TYPE = "feat";

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

        VttgFeatData result = VttgFeatData.builder()
                .type(FEAT_DATA_TYPE)
                .skillProficiencies(emptyToNull(skills))
                .armorProficiencies(emptyToNull(armor))
                .weaponProficiencies(emptyToNull(weapons))
                .damageDefenses(damageDefenses(sourceModifiers))
                .conditionImmunities(sourceModifiers == null ? null
                        : emptyToNull(VttgDictionaries.conditions(sourceModifiers.getConditionImmunities())))
                .darkvision(darkvision(sourceModifiers))
                .abilityScoreIncrease(abilityScoreIncrease(mechanics))
                .modifiers(featModifiers(sourceModifiers))
                .choices(choices(mechanics == null ? null : mechanics.getChoices()))
                .prerequisite(prerequisite)
                .build();

        return isEmpty(result) ? null : result;
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
                trimmed(bonus.getFromChoiceKey()));
    }

    private boolean isEmpty(VttgFeatData featData) {
        return featData.getSkillProficiencies() == null
                && featData.getArmorProficiencies() == null
                && featData.getWeaponProficiencies() == null
                && featData.getDamageDefenses() == null
                && featData.getConditionImmunities() == null
                && featData.getDarkvision() == null
                && featData.getAbilityScoreIncrease() == null
                && featData.getModifiers() == null
                && featData.getChoices() == null
                && featData.getPrerequisite() == null;
    }

    /** Разобранное требование черты; {@code null} — требований нет. */
    public VttgFeatPrerequisite prerequisite(Feat feat) {
        FeatPrerequisite source = feat.getPrerequisiteDetails();
        if (source == null) {
            return null;
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
                .custom(trimmed(source.getCustom()))
                .build();

        // Требование, из которого ничего не перевелось, выводить незачем: потребитель
        // всё равно покажет человекочитаемую строку из описания
        return isEmpty(result) ? null : result;
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
                && prerequisite.getCustom() == null;
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
     * Владение инструментами. Навыки, оружие и доспехи уезжают в {@code featData} —
     * их лист применяет сам; инструменты остаются здесь, потому что применить их нечем:
     * словарь инструментов сайта и справочник листа расходятся.
     */
    private VttgFeatMechanics.ProficiencyGrant toolGrant(ProficiencyGrant grant) {
        if (grant == null) {
            return null;
        }
        List<VttgEntityRef> tools = refs(grant.getTools());

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
                Boolean.TRUE.equals(source.getInitiativeProficiencyBonus()) ? Boolean.TRUE : null);

        return isEmpty(result) ? null : result;
    }

    private boolean isEmpty(VttgFeatData.Modifiers modifiers) {
        return modifiers.hitPoints() == null && modifiers.speed() == null
                && modifiers.armorClassBonus() == null && modifiers.senses() == null
                && modifiers.telepathyRange() == null
                && modifiers.resistanceFromChoiceKey() == null
                && modifiers.initiativeProficiencyBonus() == null;
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
                    options(choice.getOptions()),
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

    private List<VttgFeatMechanics.Option> options(List<ChoiceOption> options) {
        if (CollectionUtils.isEmpty(options)) {
            return null;
        }
        List<VttgFeatMechanics.Option> result = new ArrayList<>();
        for (ChoiceOption option : options) {
            if (option == null || !StringUtils.hasText(option.getValue())) {
                continue;
            }
            result.add(new VttgFeatMechanics.Option(option.getValue().trim(),
                    trimmed(option.getName())));
        }
        return emptyToNull(result);
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
        String castingTime = source.getCastingTime() == null ? null
                : source.getCastingTime().name().toLowerCase(Locale.ROOT);

        if (source.getLevel() == null && source.getMaxLevel() == null && schools.isEmpty()
                && classes == null && castingTime == null) {
            return null;
        }
        return new VttgFeatMechanics.SpellFilter(source.getLevel(), source.getMaxLevel(),
                emptyToNull(schools), classes, castingTime);
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
