package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.beastiary.model.QCreature;
import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.glossary.model.QGlossary;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class GlossaryQueryDslSearchService extends AbstractQueryDslSearchService<Glossary, QGlossary> {
    private static final QGlossary GLOSSARY = QGlossary.glossary;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{GLOSSARY.tagCategory.asc(), GLOSSARY.name.asc()};

    public GlossaryQueryDslSearchService(GlossaryFilterService glossaryFilterService, EntityManager entityManager) {
        super(glossaryFilterService, entityManager, GLOSSARY);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder(String sort) {
        if (StringUtils.isBlank(sort)) {
            return ORDER;
        }

        String[] parts = sort.split(",");
        String field = parts[0].trim().toLowerCase();
        boolean descending = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim());

        OrderSpecifier<?> order = switch (field) {
            case "name" -> descending
                    ? QGlossary.glossary.name.desc()
                    : QGlossary.glossary.name.asc();
            case "english" -> descending
                    ? QCreature.creature.english.desc()
                    : QGlossary.glossary.english.asc();
            case "category" -> descending
                    ? QGlossary.glossary.tagCategory.desc()
                    : QGlossary.glossary.tagCategory.asc();
            default ->
                // fallback на name
                    QCreature.creature.name.asc();
        };

        return new OrderSpecifier[]{order};
    }
}
