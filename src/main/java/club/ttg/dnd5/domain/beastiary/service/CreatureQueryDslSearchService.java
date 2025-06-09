package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.QCreature;
import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

@Service
public class CreatureQueryDslSearchService extends AbstractQueryDslSearchService<Creature, QCreature> {
    private static final QCreature CREATURE = QCreature.creature;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{CREATURE.experience.asc(), CREATURE.name.asc()};

    public CreatureQueryDslSearchService(CreatureFilterService creatureFilterService,
                                         EntityManager entityManager) {
        super(creatureFilterService, entityManager, CREATURE);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return ORDER;
    }
}
