package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.CharacterClassListItem;
import club.ttg.dnd5.domain.character_class.model.QCharacterClassListItem;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.sql.JPASQLQuery;
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
    private static final QCharacterClassListItem LIST_ITEM =
            new QCharacterClassListItem(CLASS.getMetadata().getName());

    public ClassQueryDslSearchService(EntityManager entityManager) {
        super (entityManager, CLASS);
    }

    @Override
    protected BooleanExpression buildSourcePredicate(final List<String> values) {
        PathBuilder<Object> characterClass = new PathBuilder<>(Object.class, CLASS.getMetadata());
        return characterClass.getString("source").in(values);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return ORDER;
    }

    /**
     * Поиск для списков классов и подклассов: те же условия, порядок и пагинация, что у
     * {@link #search(BooleanBuilder, int, int)}, но из таблицы читаются только поля краткого
     * ответа. Запись списка стоит под тем же псевдонимом, что и полная запись, поэтому
     * условия, заданные по {@link QCharacterClass}, применимы без изменений.
     */
    public List<CharacterClassListItem> searchListItems(final BooleanBuilder predicate, final int page,
                                                        final int size) {
        JPASQLQuery<CharacterClassListItem> query = new JPASQLQuery<>(entityManager, dialect);

        return query.select(LIST_ITEM)
                .from(LIST_ITEM)
                .where(predicate)
                .orderBy(getOrder())
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

}
