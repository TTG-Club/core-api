package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.spell.model.QSpell;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.domain.spell.rest.dto.SpellQueryRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

import java.util.Collection;

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

        // Тип урона (JSONB-массив)
        PredicateUtils.applyJsonbEnumArrayFilter(builder, request.getDamageType(), "damage_type");

        // Тип лечения (JSONB-массив)
        PredicateUtils.applyJsonbEnumArrayFilter(builder, request.getHealingType(), "healing_type");

        // Состояния (JSONB-массив)
        PredicateUtils.applyJsonbEnumArrayFilter(builder, request.getCondition(), "condition");

        // Спасброски (JSONB-массив)
        PredicateUtils.applyJsonbEnumArrayFilter(builder, request.getSavingThrow(), "saving_throw");

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

        // Длительность (JSONB-массив)
        PredicateUtils.applyJsonbTimeFilter(builder, request.getDuration(), "duration");

        // Источники
        PredicateUtils.applySourcesFilter(builder, request.getSource(), "spell", "source");

        return builder;
    }
}
