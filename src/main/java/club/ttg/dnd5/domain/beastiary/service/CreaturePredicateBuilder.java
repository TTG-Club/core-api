package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.QCreature;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureQueryRequest;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

import java.util.Collection;

/**
 * Построитель предикатов QueryDSL для поиска существ.
 * Использует {@link club.ttg.dnd5.dto.base.filters.QueryFilter}-based API.
 */
@UtilityClass
public class CreaturePredicateBuilder
{
    private static final QCreature Q = QCreature.creature;
    private static final StringPath ALIGNMENT_PATH = Expressions.stringPath("alignment");

    /**
     * Строит полный предикат на основе {@link CreatureQueryRequest}.
     *
     * @param request     типизированный запрос фильтрации
     * @param traitValues резолвленные оригинальные значения traits (из хэшей)
     * @param tagValues   резолвленные оригинальные значения tags (из хэшей)
     * @return {@link BooleanBuilder} для передачи в SearchService
     */
    public BooleanBuilder build(final CreatureQueryRequest request,
                                 final Collection<String> traitValues,
                                 final Collection<String> tagValues)
    {
        BooleanBuilder builder = new BooleanBuilder();

        // Скрытые сущности
        builder.and(Q.isHiddenEntity.isFalse());

        // Текстовый поиск
        builder.and(PredicateUtils.buildTextSearch(
                request.getSearch(),
                Q.name, Q.english, Q.alternative
        ));

        // Тип существа (JSONB: types->'values' @> '["BEAST"]'::jsonb)
        PredicateUtils.applyJsonbNestedEnumArrayFilter(builder, request.getType(), "types", "values");

        // Размер (JSONB: sizes->'values' @> '["MEDIUM"]'::jsonb)
        PredicateUtils.applyJsonbNestedEnumArrayFilter(builder, request.getSize(), "sizes", "values");

        // Мировоззрение (enum as STRING column)
        PredicateUtils.applyFilterEnum(builder, request.getAlignment(), ALIGNMENT_PATH, Alignment.class);

        // Уровень опасности (по experience)
        PredicateUtils.applyFilter(builder, request.getCr(), Q.experience);

        // Место обитания (JSONB: section->'habitats' @> '["FOREST"]'::jsonb)
        PredicateUtils.applyJsonbNestedEnumArrayFilter(builder, request.getHabitat(), "section", "habitats");

        // Чувства (JSONB: senses->>'key')
        PredicateUtils.applyJsonbSenseFilterQuery(builder, request.getSenses(), "senses");

        // Умения (JSONB: traits[].name) — хэши резолвлены в traitValues
        PredicateUtils.applyJsonbNamedArrayFilter(builder, request.getTraits(), "traits", "name", traitValues);

        // Тег типа (JSONB types->text + name ILIKE) — хэши резолвлены в tagValues
        PredicateUtils.applyJsonbTagFilterQuery(builder, request.getTag(), "types", Q.name, tagValues);

        // Логово
        if (request.getLair() != null && request.getLair().isActive())
        {
            if (request.getLair().isExclude())
            {
                builder.and(Expressions.booleanTemplate(
                        "lair is null or lair ->> 'name' is null or btrim(lair ->> 'name') = ''"));
            }
            else
            {
                builder.and(Expressions.booleanTemplate(
                        "lair is not null and lair ->> 'name' is not null and btrim(lair ->> 'name') <> ''"));
            }
        }

        // Легендарное действие
        if (request.getLegendaryAction() != null && request.getLegendaryAction().isActive())
        {
            if (request.getLegendaryAction().isExclude())
            {
                builder.and(Expressions.booleanTemplate(
                        "legendary_action < 1 or legendary_action is null"));
            }
            else
            {
                builder.and(Expressions.booleanTemplate(
                        "legendary_action >= 1"));
            }
        }

        // 2-state источники
        PredicateUtils.applySourcesFilter(builder, request.getSource(), "creature", "source");

        return builder;
    }
}
