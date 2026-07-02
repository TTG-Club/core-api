package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.QCreature;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureGrouping;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureSorting;
import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.sql.JPASQLQuery;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreatureQueryDslSearchService extends AbstractQueryDslSearchService<Creature, QCreature>
{
    private static final QCreature CREATURE = QCreature.creature;
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

        if (grouping == CreatureGrouping.CHALLENGE_RATING
                || sorting == CreatureSorting.CHALLENGE_RATING)
        {
            return grouping == CreatureGrouping.TYPE
                    ? new OrderSpecifier[]{typeOrder, CREATURE.experience.asc().nullsFirst(), nameOrder, CREATURE.url.asc()}
                    : new OrderSpecifier[]{CREATURE.experience.asc().nullsFirst(), nameOrder, CREATURE.url.asc()};
        }

        return grouping == CreatureGrouping.TYPE
                ? new OrderSpecifier[]{typeOrder, nameOrder, CREATURE.url.asc()}
                : new OrderSpecifier[]{nameOrder, CREATURE.url.asc()};
    }
}
