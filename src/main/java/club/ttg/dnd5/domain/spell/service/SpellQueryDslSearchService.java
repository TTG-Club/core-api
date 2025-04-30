package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import club.ttg.dnd5.domain.spell.model.QSpell;
import club.ttg.dnd5.domain.spell.model.Spell;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

@Service
public class SpellQueryDslSearchService extends AbstractQueryDslSearchService<Spell, QSpell> {
    private static final QSpell SPELL = QSpell.spell;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{SPELL.level.asc(), SPELL.name.asc()};

    public SpellQueryDslSearchService(SpellFilterService spellFilterService, EntityManager entityManager) {
        super(spellFilterService, entityManager, SPELL);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return ORDER;
    }
}
