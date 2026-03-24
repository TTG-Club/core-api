package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.QCharacterClass;
import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassQueryDslSearchService extends AbstractQueryDslSearchService<CharacterClass, QCharacterClass> {
    private static final QCharacterClass CLASS = QCharacterClass.characterClass;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{CLASS.name.asc()};

    public ClassQueryDslSearchService(EntityManager entityManager) {
        super (entityManager, CLASS);
    }

    @Override
    protected BooleanExpression buildSourcePredicate(final List<String> values) {
        PathBuilder<Object> magicItem = new PathBuilder<>(Object.class, "class");
        return magicItem.getString("source").in(values);    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return ORDER;
    }

}
