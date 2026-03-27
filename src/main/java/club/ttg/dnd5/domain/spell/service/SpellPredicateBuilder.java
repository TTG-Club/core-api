package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.spell.model.QSpell;
import club.ttg.dnd5.domain.spell.rest.dto.SpellQueryRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SpellPredicateBuilder
{
    private static final QSpell Q = QSpell.spell;
    private static final StringPath SCHOOL_PATH = Expressions.stringPath("school");

    public BooleanBuilder build(final SpellQueryRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(Q.isHiddenEntity.isFalse());
        builder.and(PredicateUtils.buildTextSearch(request.getSearch(), Q.name, Q.english, Q.alternative));

        // Школа магии (enum as STRING column)
        PredicateUtils.applyFilterEnum(builder, request.getSchool(), SCHOOL_PATH);

        // Уровень заклинания
        PredicateUtils.applyFilter(builder, request.getLevel(), Q.level);

        // Классы (ManyToMany: classAffiliation.url)
        if (request.getClassName() != null && request.getClassName().isActive())
        {
            if (request.getClassName().isExclude())
            {
                builder.and(Q.classAffiliation.any().url.notIn(request.getClassName().getValues()));
            }
            else if (request.getClassName().isUnion())
            {
                BooleanBuilder orBuilder = new BooleanBuilder();
                for (String url : request.getClassName().getValues())
                {
                    orBuilder.or(Q.classAffiliation.any().url.eq(url));
                }
                builder.and(orBuilder);
            }
            else
            {
                builder.and(Q.classAffiliation.any().url.in(request.getClassName().getValues()));
            }
        }

        // Подклассы (ManyToMany: subclassAffiliation.url)
        if (request.getSubclassName() != null && request.getSubclassName().isActive())
        {
            if (request.getSubclassName().isExclude())
            {
                builder.and(Q.subclassAffiliation.any().url.notIn(request.getSubclassName().getValues()));
            }
            else if (request.getSubclassName().isUnion())
            {
                BooleanBuilder orBuilder = new BooleanBuilder();
                for (String url : request.getSubclassName().getValues())
                {
                    orBuilder.or(Q.subclassAffiliation.any().url.eq(url));
                }
                builder.and(orBuilder);
            }
            else
            {
                builder.and(Q.subclassAffiliation.any().url.in(request.getSubclassName().getValues()));
            }
        }

        // Тип урона (JSONB-массив)
        PredicateUtils.applyJsonbEnumArrayFilter(builder, request.getDamageType(), "damage_type");

        // Тип лечения (JSONB-массив)
        PredicateUtils.applyJsonbEnumArrayFilter(builder, request.getHealingType(), "healing_type");

        // Состояния (JSONB-массив)
        PredicateUtils.applyJsonbEnumArrayFilter(builder, request.getCondition(), "condition");

        // Спасброски (JSONB-массив)
        PredicateUtils.applyJsonbEnumArrayFilter(builder, request.getSavingThrow(), "saving_throw");

        // Ритуал
        PredicateUtils.applySingletonFilter(builder, request.getRitual(),
                "exists (select 1 from jsonb_array_elements(casting_time) as elem where (elem @> '{\"unit\": \"RITUAL\"}'))",
                "NOT exists (select 1 from jsonb_array_elements(casting_time) as elem where (elem @> '{\"unit\": \"RITUAL\"}'))");

        // Концентрация
        PredicateUtils.applySingletonFilter(builder, request.getConcentration(),
                "exists (select 1 from jsonb_array_elements(duration) as elem where (elem @> '{\"concentration\": true}'))",
                "NOT exists (select 1 from jsonb_array_elements(duration) as elem where (elem @> '{\"concentration\": true}'))");

        // Улучшается с уровнем ячейки
        PredicateUtils.applySingletonFilter(builder, request.getUpcastable(), Q.upcastable);

        // Источники
        PredicateUtils.applySourcesFilter(builder, request.getSource(), "spell", "source");

        return builder;
    }
}
