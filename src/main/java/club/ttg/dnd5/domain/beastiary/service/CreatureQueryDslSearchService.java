package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.QCreature;
import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreatureQueryDslSearchService extends AbstractQueryDslSearchService<Creature, QCreature>
{
    private static final QCreature CREATURE = QCreature.creature;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{
            CREATURE.experience.asc(),
            CREATURE.name.asc(),
            CREATURE.url.asc()
    };

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
        return ORDER;
    }
}
