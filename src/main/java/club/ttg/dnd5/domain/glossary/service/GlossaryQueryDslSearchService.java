package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.glossary.model.QGlossary;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

@Service
public class GlossaryQueryDslSearchService extends AbstractQueryDslSearchService<Glossary, QGlossary> {
    private static final QGlossary GLOSSARY = QGlossary.glossary;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{GLOSSARY.tagCategory.asc(), GLOSSARY.name.asc()};

    public GlossaryQueryDslSearchService(GlossaryFilterService glossaryFilterService, EntityManager entityManager) {
        super(glossaryFilterService, entityManager, GLOSSARY);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return ORDER;
    }
}
