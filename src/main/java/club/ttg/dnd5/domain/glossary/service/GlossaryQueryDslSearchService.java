package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.glossary.model.QGlossary;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GlossaryQueryDslSearchService extends AbstractQueryDslSearchService<Glossary, QGlossary> {
    private static final QGlossary GLOSSARY = QGlossary.glossary;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{GLOSSARY.tagCategory.asc(), GLOSSARY.name.asc()};

    public GlossaryQueryDslSearchService(EntityManager entityManager) {
        super(entityManager, GLOSSARY);
    }

    @Override
    protected BooleanExpression buildSourcePredicate(final List<String> values) {
        PathBuilder<Object> magicItem = new PathBuilder<>(Object.class, "glossary");
        return magicItem.getString("source").in(values);    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return ORDER;
    }
}
