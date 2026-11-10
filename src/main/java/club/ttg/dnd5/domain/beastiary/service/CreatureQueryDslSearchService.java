package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.QCreature;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureGrouping;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureSorting;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.sql.JPASQLQuery;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CreatureQueryDslSearchService extends AbstractQueryDslSearchService<Creature, QCreature>
{
    private static final QCreature CREATURE = QCreature.creature;

    /**
     * Пользовательский рейтинг существа из общей таблицы {@code rating} (раздел {@code bestiary}, ключ — url):
     * средняя оценка, затем число оценок, обе по убыванию. Существа без оценок — в конце.
     */
    private static final String RATING_SUBQUERY = "(select %s from rating r where r.section = '"
            + SectionType.BESTIARY.getValue() + "' and r.url = {0})";

    private static final OrderSpecifier<Double> RATING_VALUE_ORDER = Expressions
            .numberTemplate(Double.class, RATING_SUBQUERY.formatted("coalesce(avg(r.value), 0)"), CREATURE.url)
            .desc();

    private static final OrderSpecifier<Long> RATING_COUNT_ORDER = Expressions
            .numberTemplate(Long.class, RATING_SUBQUERY.formatted("count(r.value)"), CREATURE.url)
            .desc();

    public CreatureQueryDslSearchService(EntityManager entityManager)
    {
        super(entityManager, CREATURE);
    }

    @Override
    protected BooleanExpression buildSourcePredicate(final List<String> values)
    {
        PathBuilder<Object> creature = new PathBuilder<>(Object.class, "creature");
        return creature.getString("source").in(values);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder()
    {
        return getOrder(CreatureGrouping.CHALLENGE_RATING, CreatureSorting.CHALLENGE_RATING);
    }

    public List<Creature> search(final BooleanBuilder predicate, final int page, final int size,
                                 final CreatureGrouping grouping, final CreatureSorting sorting)
    {
        JPASQLQuery<Creature> query = new JPASQLQuery<>(entityManager, dialect);

        return query.select(entityPath)
                .from(entityPath)
                .where(predicate)
                .orderBy(getOrder(grouping, sorting))
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    OrderSpecifier<?>[] getOrder(final CreatureGrouping grouping, final CreatureSorting sorting)
    {
        OrderSpecifier<?> nameOrder = sorting == CreatureSorting.ENGLISH
                ? CREATURE.english.asc()
                : CREATURE.name.asc();

        OrderSpecifier<?> typeOrder = Expressions
                .stringTemplate("cast({0} as text)", CREATURE.types)
                .asc();

        OrderSpecifier<?> challengeRatingOrder = CREATURE.experience.asc().nullsFirst();

        List<OrderSpecifier<?>> order = new ArrayList<>();

        if (grouping == CreatureGrouping.TYPE)
        {
            order.add(typeOrder);
        }

        if (grouping == CreatureGrouping.CHALLENGE_RATING
                || sorting == CreatureSorting.CHALLENGE_RATING)
        {
            order.add(challengeRatingOrder);
        }

        if (sorting == CreatureSorting.RATING)
        {
            order.add(RATING_VALUE_ORDER);
            order.add(RATING_COUNT_ORDER);
        }

        order.add(nameOrder);
        order.add(CREATURE.url.asc());

        return order.toArray(OrderSpecifier[]::new);
    }
}
