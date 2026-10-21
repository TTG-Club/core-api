package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.common.dictionary.HealingType;
import club.ttg.dnd5.domain.spell.model.QSpell;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.domain.spell.model.enums.SpellComponent;
import club.ttg.dnd5.domain.spell.rest.dto.SpellQueryRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class SpellPredicateBuilder {
    private static final QSpell Q = QSpell.spell;
    private static final StringPath SCHOOL_PATH = Expressions.stringPath("school");

    public BooleanBuilder build(final SpellQueryRequest request,
            Collection<String> classes,
            Collection<String> subclasses) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(Q.isHiddenEntity.isFalse());
        builder.and(PredicateUtils.buildTextSearch(request.getSearch(), Q.name, Q.english, Q.alternative));

        if (request.getClassGroup() != null && !request.getClassGroup().isBlank()) {
            if (SpellQueryRequest.WITHOUT_CLASS_GROUP.equals(request.getClassGroup())) {
                builder.and(Expressions.booleanTemplate(
                        "not exists (select 1 from spell_class_affiliation sca where sca.spell_url = {0}.url)",
                        Q));
            } else {
                builder.and(Expressions.booleanTemplate(
                        "exists (select 1 from spell_class_affiliation sca where sca.spell_url = {0}.url and sca.class_affiliation_url = {1})",
                        Q,
                        request.getClassGroup()));
            }
        }

        // Школа магии (enum as STRING column)
        PredicateUtils.applyFilterEnum(builder, request.getSchool(), SCHOOL_PATH, MagicSchool.class);

        // Уровень заклинания
        PredicateUtils.applyFilter(builder, request.getLevel(), Q.level);

        // Классы + Подклассы: объединяются через ИЛИ (OR).
        // Exclude-предикаты остаются через И (AND) в основном builder.
        BooleanBuilder classInclude = new BooleanBuilder();
        BooleanBuilder subclassInclude = new BooleanBuilder();

        // Классы (ManyToMany: classAffiliation)
        if (request.getClassName() != null && request.getClassName().isActive() && !classes.isEmpty()) {
            if (request.getClassName().isExclude()) {
                for (String url : classes) {
                    builder.and(com.querydsl.core.types.dsl.Expressions.booleanTemplate(
                            "not exists (select 1 from spell_class_affiliation sca where sca.spell_url = {0}.url and sca.class_affiliation_url = {1})",
                            Q, url));
                }
            } else if (request.getClassName().isUnion()) {
                for (String url : classes) {
                    classInclude.and(com.querydsl.core.types.dsl.Expressions.booleanTemplate(
                            "exists (select 1 from spell_class_affiliation sca where sca.spell_url = {0}.url and sca.class_affiliation_url = {1})",
                            Q, url));
                }
            } else {
                String inClause = classes.stream().map(u -> "'" + u.replace("'", "''") + "'")
                        .collect(java.util.stream.Collectors.joining(","));
                classInclude.and(com.querydsl.core.types.dsl.Expressions.booleanTemplate(
                        "exists (select 1 from spell_class_affiliation sca where sca.spell_url = {0}.url and sca.class_affiliation_url in ("
                                + inClause + "))",
                        Q));
            }
        }

        // Подклассы (ManyToMany: subclassAffiliation)
        if (request.getSubclassName() != null && request.getSubclassName().isActive() && !subclasses.isEmpty()) {
            if (request.getSubclassName().isExclude()) {
                for (String url : subclasses) {
                    builder.and(com.querydsl.core.types.dsl.Expressions.booleanTemplate(
                            "not exists (select 1 from spell_subclass_affiliation ssa where ssa.spell_url = {0}.url and ssa.subclass_affiliation_url = {1})",
                            Q, url));
                }
            } else if (request.getSubclassName().isUnion()) {
                for (String url : subclasses) {
                    subclassInclude.and(com.querydsl.core.types.dsl.Expressions.booleanTemplate(
                            "exists (select 1 from spell_subclass_affiliation ssa where ssa.spell_url = {0}.url and ssa.subclass_affiliation_url = {1})",
                            Q, url));
                }
            } else {
                String inClause = subclasses.stream().map(u -> "'" + u.replace("'", "''") + "'")
                        .collect(java.util.stream.Collectors.joining(","));
                subclassInclude.and(com.querydsl.core.types.dsl.Expressions.booleanTemplate(
                        "exists (select 1 from spell_subclass_affiliation ssa where ssa.spell_url = {0}.url and ssa.subclass_affiliation_url in ("
                                + inClause + "))",
                        Q));
            }
        }

        // Объединение классов и подклассов через ИЛИ (OR)
        if (classInclude.hasValue() || subclassInclude.hasValue()) {
            BooleanBuilder combined = new BooleanBuilder();
            if (classInclude.hasValue()) {
                combined.or(classInclude);
            }
            if (subclassInclude.hasValue()) {
                combined.or(subclassInclude);
            }
            builder.and(combined);
        }

        // Тип урона (JSONB-массив): типы из формул дописываются в поле при сохранении
        PredicateUtils.applyJsonbNestedEnumArrayFilter(builder, request.getDamageType(), "effect", "damageTypes");

        // Тип лечения (JSONB-массив)
        applyHealingTypeFilter(builder, request.getHealingType());

        // Состояния (JSONB-массив)
        PredicateUtils.applyJsonbNestedEnumArrayFilter(builder, request.getCondition(), "effect", "conditions");

        // Спасброски (JSONB-массив)
        PredicateUtils.applyJsonbNestedEnumArrayFilter(builder, request.getSavingThrow(), "effect", "savingThrows");

        // Ритуал (QueryFilter: ritual=1 / ritual=1&ritual_mode=1)
        if (request.getRitual() != null && request.getRitual().isActive()) {
            if (request.getRitual().isExclude()) {
                builder.and(Expressions.booleanTemplate(
                        "NOT exists (select 1 from jsonb_array_elements(casting_time) as elem where (elem @> '{\"unit\": \"RITUAL\"}'))"));
            } else {
                builder.and(Expressions.booleanTemplate(
                        "exists (select 1 from jsonb_array_elements(casting_time) as elem where (elem @> '{\"unit\": \"RITUAL\"}'))"));
            }
        }

        // Концентрация (QueryFilter: concentration=1 /
        // concentration=1&concentration_mode=1)
        if (request.getConcentration() != null && request.getConcentration().isActive()) {
            if (request.getConcentration().isExclude()) {
                builder.and(Expressions.booleanTemplate(
                        "NOT exists (select 1 from jsonb_array_elements(duration) as elem where (elem @> '{\"concentration\": true}'))"));
            } else {
                builder.and(Expressions.booleanTemplate(
                        "exists (select 1 from jsonb_array_elements(duration) as elem where (elem @> '{\"concentration\": true}'))"));
            }
        }

        // Улучшается с уровнем ячейки (QueryFilter: upcastable=1 /
        // upcastable=1&upcastable_mode=1)
        if (request.getUpcastable() != null && request.getUpcastable().isActive()) {
            if (request.getUpcastable().isExclude()) {
                builder.and(Q.upcastable.isFalse());
            } else {
                builder.and(Q.upcastable.isTrue());
            }
        }

        // Время накладывания (JSONB-массив)
        PredicateUtils.applyJsonbTimeFilter(builder, request.getCastingTime(), "casting_time");

        // Дистанция наложения (JSONB-массив)
        PredicateUtils.applyJsonbMeasureFilter(builder, request.getDistance(), "range");

        // Длительность (JSONB-массив)
        PredicateUtils.applyJsonbTimeFilter(builder, request.getDuration(), "duration");

        // Компоненты (колонки v, s и JSONB-объект m)
        applyComponentFilter(builder, request.getComponent());

        // Область воздействия (JSONB-объект, поле "type")
        PredicateUtils.applyJsonbObjectEnumFieldFilter(
                builder, request.getAreaOfEffectType(), "(effect->'areaOfEffect')", "type");

        // Тип атаки (enum as STRING column)
        PredicateUtils.applyJsonbObjectEnumFieldFilter(builder, request.getAttackType(), "effect", "attackType");

        // Источники
        PredicateUtils.applySourcesFilter(builder, request.getSource(), "spell", "source");

        // Версия SRD
        PredicateUtils.applyStringFilter(builder, request.getSrdVersion(), Q.srdVersion);

        return builder;
    }

    /**
     * Фильтр по компонентам. «С ценой» и «Расходуемый» не самостоятельные условия,
     * а уточнения материального компонента: они объединяются с ним через И, даже
     * если сама группа работает через ИЛИ. Исключение отбрасывает строки, где
     * условие истинно; null-значения колонок при этом не выбрасывают заклинание.
     */
    private void applyComponentFilter(BooleanBuilder builder, QueryFilter<SpellComponent> filter) {
        if (filter == null || !filter.isActive()) {
            return;
        }

        if (filter.isExclude()) {
            for (var value : filter.getValues()) {
                builder.and(Expressions.booleanTemplate("((" + componentCondition(value) + ") is not true)"));
            }
            return;
        }

        List<String> conditions = new ArrayList<>();
        List<SpellComponent> materialProperties = new ArrayList<>();
        for (var value : filter.getValues()) {
            if (value.isMaterialProperty()) {
                materialProperties.add(value);
            } else if (value != SpellComponent.MATERIAL) {
                conditions.add(componentCondition(value));
            }
        }

        if (filter.getValues().contains(SpellComponent.MATERIAL) || !materialProperties.isEmpty()) {
            conditions.add(Stream.concat(Stream.of(SpellComponent.MATERIAL), materialProperties.stream())
                    .map(SpellPredicateBuilder::componentCondition)
                    .collect(Collectors.joining(" and ")));
        }

        String joined = conditions.stream()
                .map(condition -> "(" + condition + ")")
                .collect(Collectors.joining(filter.isUnion() ? " and " : " or "));
        builder.and(Expressions.booleanTemplate("(" + joined + ")"));
    }

    private String componentCondition(SpellComponent component) {
        return switch (component) {
            case VERBAL -> "v is true";
            case SOMATIC -> "s is true";
            // Пустой текст материального компонента при сохранении превращается в null
            case MATERIAL -> "coalesce(m->>'text', '') <> ''";
            case WITH_COST -> "(m->>'withCost') = 'true'";
            case CONSUMABLE -> "(m->>'consumable') = 'true'";
        };
    }

    private void applyHealingTypeFilter(BooleanBuilder builder, QueryFilter<HealingType> filter) {
        if (filter == null || !filter.isActive()) {
            return;
        }

        if (filter.isExclude()) {
            for (var value : filter.getValues()) {
                builder.and(healingTypeNotExists(value));
            }
        } else if (filter.isUnion()) {
            for (var value : filter.getValues()) {
                builder.and(healingTypeExists(value));
            }
        } else {
            BooleanBuilder orBuilder = new BooleanBuilder();
            for (var value : filter.getValues()) {
                orBuilder.or(healingTypeExists(value));
            }
            builder.and(orBuilder);
        }
    }

    private Predicate healingTypeExists(HealingType value) {
        if (value == HealingType.HEALING) {
            return Expressions.booleanTemplate(
                    "("
                            + "exists (select 1 from jsonb_array_elements_text(coalesce(effect->'healingTypes', '[]'::jsonb)) as healing_type where healing_type = {0})"
                            + " or exists (select 1 from jsonb_array_elements_text(coalesce(effect->'damageFormulas', '[]'::jsonb)) as formula where formula like {1} and formula not like {2})"
                            + ")",
                    value.name(),
                    "%@heal%",
                    "%heal.temp%");
        }
        return Expressions.booleanTemplate(
                "("
                        + "exists (select 1 from jsonb_array_elements_text(coalesce(effect->'healingTypes', '[]'::jsonb)) as healing_type where healing_type = {0})"
                        + " or exists (select 1 from jsonb_array_elements_text(coalesce(effect->'damageFormulas', '[]'::jsonb)) as formula where formula like {1})"
                        + ")",
                value.name(),
                healingFormulaMarker(value));
    }

    private Predicate healingTypeNotExists(HealingType value) {
        if (value == HealingType.HEALING) {
            return Expressions.booleanTemplate(
                    "("
                            + "not exists (select 1 from jsonb_array_elements_text(coalesce(effect->'healingTypes', '[]'::jsonb)) as healing_type where healing_type = {0})"
                            + " and not exists (select 1 from jsonb_array_elements_text(coalesce(effect->'damageFormulas', '[]'::jsonb)) as formula where formula like {1} and formula not like {2})"
                            + ")",
                    value.name(),
                    "%@heal%",
                    "%heal.temp%");
        }
        return Expressions.booleanTemplate(
                "("
                        + "not exists (select 1 from jsonb_array_elements_text(coalesce(effect->'healingTypes', '[]'::jsonb)) as healing_type where healing_type = {0})"
                        + " and not exists (select 1 from jsonb_array_elements_text(coalesce(effect->'damageFormulas', '[]'::jsonb)) as formula where formula like {1})"
                        + ")",
                value.name(),
                healingFormulaMarker(value));
    }

    private String healingFormulaMarker(HealingType value) {
        return switch (value) {
            case HEALING -> "%@heal%";
            case TEMPORARY_HITPOINTS -> "%heal.temp%";
        };
    }
}
