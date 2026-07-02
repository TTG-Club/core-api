package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import club.ttg.dnd5.domain.spell.model.QSpell;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.rest.dto.SpellGrouping;
import club.ttg.dnd5.domain.spell.rest.dto.SpellSorting;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.sql.JPASQLQuery;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpellQueryDslSearchService extends AbstractQueryDslSearchService<Spell, QSpell>
{
    private static final QSpell SPELL = QSpell.spell;
    public SpellQueryDslSearchService(EntityManager entityManager)
    {
        super(entityManager, SPELL);
    }

    @Override
    protected BooleanExpression buildSourcePredicate(final List<String> values)
    {
        PathBuilder<Object> spell = new PathBuilder<>(Object.class, "spell");
        return spell.getString("source").in(values);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder()
    {
        return getOrder(SpellGrouping.LEVEL, SpellSorting.NAME);
    }

    public List<Spell> search(final BooleanBuilder predicate, final int page, final int size,
                              final SpellGrouping grouping, final SpellSorting sorting)
    {
        JPASQLQuery<Spell> query = new JPASQLQuery<>(entityManager, dialect);

        return query.select(entityPath)
                .from(entityPath)
                .where(predicate)
                .orderBy(getOrder(grouping, sorting))
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    OrderSpecifier<?>[] getOrder(final SpellGrouping grouping, final SpellSorting sorting)
    {
        OrderSpecifier<?> nameOrder = sorting == SpellSorting.ENGLISH
                ? SPELL.english.asc()
                : SPELL.name.asc();

        return switch (grouping)
        {
            case LEVEL -> new OrderSpecifier[]{SPELL.level.asc(), nameOrder};
            case SCHOOL -> new OrderSpecifier[]{SPELL.school.school.asc(), nameOrder};
            case CLASS, NONE -> new OrderSpecifier[]{nameOrder};
        };
    }
}
