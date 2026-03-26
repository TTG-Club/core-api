package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.magic.model.QMagicItem;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemSearchRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

/**
 * Построитель предикатов QueryDSL для поиска магических предметов.
 * Заменяет 3 legacy FilterGroup.
 */
@UtilityClass
public class MagicItemPredicateBuilder
{
    private static final QMagicItem Q = QMagicItem.magicItem;
    private static final StringPath CATEGORY_PATH = Expressions.stringPath("category");
    private static final StringPath RARITY_PATH = Expressions.stringPath("rarity");

    public BooleanBuilder build(final MagicItemSearchRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(Q.isHiddenEntity.isFalse());

        builder.and(PredicateUtils.buildTextSearch(
                request.getText(),
                Q.name, Q.english, Q.alternative
        ));

        // Категория (enum STRING)
        PredicateUtils.applyThreeStateEnum(builder, request.getCategory(), CATEGORY_PATH);

        // Редкость (enum STRING)
        PredicateUtils.applyThreeStateEnum(builder, request.getRarity(), RARITY_PATH);

        // Настройка (JSONB attunement is not null and attunement->>'required' = 'true')
        PredicateUtils.applySingletonNative(builder, request.getAttunement(),
                "attunement is not null and (attunement->>'required') = 'true'",
                "attunement is null or (attunement->>'required') != 'true'"
        );

        // Заряды (charges > 0)
        PredicateUtils.applySingletonNative(builder, request.getCharges(),
                "charges is not null and charges > 0",
                "charges is null or charges <= 0"
        );

        // Проклятие
        PredicateUtils.applySingletonNative(builder, request.getCurse(),
                "curse = true",
                "curse = false or curse is null"
        );

        // Источники
        PredicateUtils.applySources(builder, request.getEnabledSources(), "magic_item", "source");

        return builder;
    }
}
