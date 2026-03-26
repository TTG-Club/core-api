package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.spell.model.QSpell;
import club.ttg.dnd5.domain.spell.rest.dto.SpellSearchRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

/**
 * Построитель предикатов QueryDSL для поиска заклинаний.
 * Заменяет 10 legacy FilterGroup классов одним утилитным классом.
 */
@UtilityClass
public class SpellPredicateBuilder
{
    private static final QSpell Q = QSpell.spell;
    private static final StringPath SCHOOL_PATH = Expressions.stringPath("school");

    /**
     * Строит полный предикат на основе {@link SpellSearchRequest}.
     *
     * @param request типизированный запрос фильтрации
     * @return {@link BooleanBuilder} для передачи в SearchService
     */
    public BooleanBuilder build(final SpellSearchRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();

        // Скрытые сущности
        builder.and(Q.isHiddenEntity.isFalse());

        // Текстовый поиск
        builder.and(PredicateUtils.buildTextSearch(
                request.getText(),
                Q.name, Q.english, Q.alternative
        ));

        // Школа магии (Embedded: school.school — хранится как String в столбце "school")
        PredicateUtils.applyThreeStateEnum(builder, request.getSchool(), SCHOOL_PATH);

        // Уровень заклинания
        PredicateUtils.applyThreeState(builder, request.getLevel(), Q.level);

        // Классы (ManyToMany: classAffiliation.url)
        if (request.getClassName() != null && request.getClassName().isActive())
        {
            if (!request.getClassName().getInclude().isEmpty())
            {
                builder.and(Q.classAffiliation.any().url.in(request.getClassName().getInclude()));
            }

            if (!request.getClassName().getExclude().isEmpty())
            {
                builder.and(Q.classAffiliation.any().url.notIn(request.getClassName().getExclude()));
            }
        }

        // Подклассы (ManyToMany: subclassAffiliation.url)
        if (request.getSubclassName() != null && request.getSubclassName().isActive())
        {
            if (!request.getSubclassName().getInclude().isEmpty())
            {
                builder.and(Q.subclassAffiliation.any().url.in(request.getSubclassName().getInclude()));
            }

            if (!request.getSubclassName().getExclude().isEmpty())
            {
                builder.and(Q.subclassAffiliation.any().url.notIn(request.getSubclassName().getExclude()));
            }
        }

        // Тип урона (JSONB-массив)
        PredicateUtils.applyJsonbEnumArray(builder, request.getDamageType(), "damage_type");

        // Тип лечения (JSONB-массив)
        PredicateUtils.applyJsonbEnumArray(builder, request.getHealingType(), "healing_type");

        // Состояния (JSONB-массив)
        PredicateUtils.applyJsonbEnumArray(builder, request.getCondition(), "condition");

        // Спасброски (JSONB-массив)
        PredicateUtils.applyJsonbEnumArray(builder, request.getSavingThrow(), "saving_throw");

        // Ритуал (JSONB: castingTime[].unit = "RITUAL")
        PredicateUtils.applySingletonNative(builder, request.getRitual(),
                "exists (select 1 from jsonb_array_elements(casting_time) as elem where (elem @> '{\"unit\": \"RITUAL\"}'))",
                "NOT exists (select 1 from jsonb_array_elements(casting_time) as elem where (elem @> '{\"unit\": \"RITUAL\"}'))"
        );

        // Концентрация (JSONB: duration[].concentration = true)
        PredicateUtils.applySingletonNative(builder, request.getConcentration(),
                "exists (select 1 from jsonb_array_elements(duration) as elem where (elem @> '{\"concentration\": true}'))",
                "NOT exists (select 1 from jsonb_array_elements(duration) as elem where (elem @> '{\"concentration\": true}'))"
        );

        // Улучшается с уровнем ячейки
        PredicateUtils.applySingleton(builder, request.getUpcastable(), Q.upcastable);

        // 2-state источники
        PredicateUtils.applySources(builder, request.getEnabledSources(), "spell", "source");

        return builder;
    }
}
