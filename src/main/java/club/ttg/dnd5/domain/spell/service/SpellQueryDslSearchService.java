package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import club.ttg.dnd5.domain.spell.model.QSpell;
import club.ttg.dnd5.domain.spell.model.Spell;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpellQueryDslSearchService extends AbstractQueryDslSearchService<Spell, QSpell>
{
    private static final QSpell SPELL = QSpell.spell;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{
            SPELL.level.asc(),
            SPELL.name.asc(),
            SPELL.url.asc()
    };

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
        return ORDER;
    }
}
