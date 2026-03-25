package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.QCreature;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureSearchRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

/**
 * Построитель предикатов QueryDSL для поиска существ.
 * Заменяет 9 legacy FilterGroup классов.
 */
@UtilityClass
public class CreaturePredicateBuilder
{
    private static final QCreature Q = QCreature.creature;
    private static final StringPath ALIGNMENT_PATH = Expressions.stringPath("alignment");

    public BooleanBuilder build(final CreatureSearchRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();

        // Скрытые сущности
        builder.and(Q.isHiddenEntity.isFalse());

        // Текстовый поиск
        builder.and(PredicateUtils.buildTextSearch(
                request.getText(),
                Q.name, Q.english, Q.alternative
        ));

        // Тип существа (JSONB: types->'values' @> '["BEAST"]'::jsonb)
        PredicateUtils.applyJsonbNestedEnumArray(builder, request.getCreatureType(), "types", "values");

        // Размер (JSONB: sizes->'values' @> '["MEDIUM"]'::jsonb)
        PredicateUtils.applyJsonbNestedEnumArray(builder, request.getCreatureSize(), "sizes", "values");

        // Мировоззрение (enum as STRING column)
        PredicateUtils.applyThreeStateEnum(builder, request.getAlignment(), ALIGNMENT_PATH);

        // Уровень опасности (по experience)
        PredicateUtils.applyThreeState(builder, request.getChallengeRating(), Q.experience);

        // Место обитания (JSONB: section->'habitats' @> '["FOREST"]'::jsonb)
        PredicateUtils.applyJsonbNestedEnumArray(builder, request.getHabitat(), "section", "habitats");

        // Чувства (JSONB: senses->>'key')
        PredicateUtils.applyJsonbSenseFilter(builder, request.getSenses(), "senses");

        // Умения (JSONB: traits[].name)
        PredicateUtils.applyJsonbNamedArray(builder, request.getTraits(), "traits", "name");

        // Тег типа (JSONB types->text + name ILIKE)
        PredicateUtils.applyJsonbTagFilter(builder, request.getTag(), "types", Q.name);

        // Логово (singleton JSONB lair)
        PredicateUtils.applySingletonNative(builder, request.getLair(),
                "lair is not null and lair ->> 'name' is not null and btrim(lair ->> 'name') <> ''",
                "lair is null or lair ->> 'name' is null or btrim(lair ->> 'name') = ''"
        );

        // Легендарное действие (singleton)
        PredicateUtils.applySingletonNative(builder, request.getLegendaryAction(),
                "legendary_action >= 1",
                "legendary_action < 1 or legendary_action is null"
        );

        // 2-state источники
        PredicateUtils.applySources(builder, request.getEnabledSources(), Q.source.acronym);

        return builder;
    }
}
