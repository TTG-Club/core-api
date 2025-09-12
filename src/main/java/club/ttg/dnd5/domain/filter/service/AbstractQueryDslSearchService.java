package club.ttg.dnd5.domain.filter.service;

import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLTemplates;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static club.ttg.dnd5.dto.base.filters.Filter.TRUE_EXPRESSION;

@RequiredArgsConstructor
public abstract class AbstractQueryDslSearchService<E, Q extends EntityPathBase<E>> {
    protected final AbstractSavedFilterService<?> savedFilterService;
    protected final EntityManager entityManager;
    protected final Q entityPath;

    protected static final SQLTemplates dialect = new PostgreSQLTemplates();

    public List<E> search(String searchLine,
                          int page,
                          int limit,
                          String[] sort,
                          SearchBody searchBody) {

        BooleanExpression predicate = getPredicate(searchLine, searchBody);

        JPASQLQuery<?> query = new JPASQLQuery<Void>(entityManager, dialect);
        return query.select(entityPath)
                .from(entityPath)
                .where(predicate)
                .orderBy(getOrder(sort))
                .limit(limit)
                .offset((long) Math.max(page - 1, 0) * limit)
                .fetch();
    }

    public long count(String searchLine, SearchBody searchBody) {
        BooleanExpression predicate = getPredicate(searchLine, searchBody);

        Long total = new JPASQLQuery<Void>(entityManager, dialect)
                .select(Expressions.numberTemplate(Long.class, "count(*)"))
                .from(entityPath)
                .where(predicate)
                .fetchOne();

        return total != null ? total : 0L;
    }

    // -------------------- Predicate --------------------

    private BooleanExpression getPredicate(String searchLine, SearchBody searchBody) {
        BooleanExpression searchExpr = TRUE_EXPRESSION;

        if (StringUtils.isNotBlank(searchLine)) {
            String raw = searchLine.trim();
            String switched = SwitchLayoutUtils.switchLayout(raw);

            String p1 = "%" + escapeLike(raw) + "%";
            String p2 = "%" + escapeLike(switched) + "%";

            PathBuilder<E> root = new PathBuilder<>(entityPath.getType(), entityPath.getMetadata());

            BooleanExpression nameLike =
                    Expressions.booleanTemplate("{0} ILIKE {1} ESCAPE '!'", root.getString("name"), Expressions.constant(p1))
                            .or(Expressions.booleanTemplate("{0} ILIKE {1} ESCAPE '!'", root.getString("name"), Expressions.constant(p2)));

            BooleanExpression englishLike =
                    Expressions.booleanTemplate("{0} ILIKE {1} ESCAPE '!'", root.getString("english"), Expressions.constant(p1))
                            .or(Expressions.booleanTemplate("{0} ILIKE {1} ESCAPE '!'", root.getString("english"), Expressions.constant(p2)));

            BooleanExpression altLike =
                    Expressions.booleanTemplate("{0} ILIKE {1} ESCAPE '!'", root.getString("alternative"), Expressions.constant(p1))
                            .or(Expressions.booleanTemplate("{0} ILIKE {1} ESCAPE '!'", root.getString("alternative"), Expressions.constant(p2)));

            searchExpr = nameLike.or(englishLike).or(altLike);
        }

        BooleanExpression filterExpr = Optional.ofNullable(searchBody)
                .map(SearchBody::getFilter)
                .orElseGet(savedFilterService::getDefaultFilterInfo)
                .getQuery();

        return searchExpr.and(filterExpr);
    }

    /**
     * Поддерживает формы:
     *  - "-field" / "+field"
     *  - "field,desc" / "field,asc"
     *  - вложенные пути "a.b.c" (по ассоциациям и @Embeddable)
     */
    protected OrderSpecifier<?>[] getOrder(String[] sort) {
        if (sort == null || sort.length == 0) {
            return getDefaultOrder();
        }

        PathBuilder<E> root = new PathBuilder<>(entityPath.getType(), entityPath.getMetadata());
        List<OrderSpecifier<?>> out = new ArrayList<>(sort.length);

        for (String token : sort) {
            if (StringUtils.isBlank(token)) {
                continue;
            }

            String key;
            Order order;

            if (token.startsWith("-")) {
                key = token.substring(1).trim();
                order = Order.DESC;
            } else if (token.startsWith("+")) {
                key = token.substring(1).trim();
                order = Order.ASC;
            } else {
                String[] parts = token.split(",", 2);
                key = parts[0].trim();
                order = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) ? Order.DESC : Order.ASC;
            }

            if (!isSortablePath(entityPath.getType(), key)) {
                // неизвестный или не сортируемый путь — пропускаем
                continue;
            }

            ComparableExpressionBase<Comparable> expr = root.getComparable(key, Comparable.class);
            out.add(new OrderSpecifier<>(order, expr));
        }

        return out.isEmpty() ? getDefaultOrder() : out.toArray(new OrderSpecifier<?>[0]);
    }

    /**
     * Дефолтная сортировка для конкретной реализации сервиса.
     */
    protected abstract OrderSpecifier<?>[] getDefaultOrder();

    /** Экранирует спецсимволы LIKE: %, _, а также сам экранирующий символ '!'. */
    private static String escapeLike(String s) {
        if (s == null) return null;
        return s.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }

    /**
     * Проверяет, что dottedPath существует в JPA-модели, и его конечный тип сортируемый.
     * Поддерживает ассоциации и @Embeddable (вложенные пути "a.b.c").
     */
    private boolean isSortablePath(Class<?> rootType, String dottedPath) {
        Metamodel mm = entityManager.getMetamodel();
        Class<?> currentType = rootType;

        String[] parts = dottedPath.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            boolean last = (i == parts.length - 1);

            final ManagedType<?> mt;
            try {
                mt = mm.managedType(currentType);
            } catch (IllegalArgumentException ex) {
                return false; // текущий тип не managed — путь недействителен
            }

            Attribute<?, ?> attr = findAttribute(mt, p);
            if (attr == null) {
                return false; // нет такого атрибута
            }

            if (attr.isAssociation()) {
                if (last) {
                    return false;
                }
                currentType = attr.getJavaType();
                continue;
            }

            if (last) {
                return isSortableJavaType(attr.getJavaType());
            } else {
                // Разрешаем продолжение только если это @Embeddable
                if (!attr.getJavaType().isAnnotationPresent(jakarta.persistence.Embeddable.class)) {
                    return false;
                }
                currentType = attr.getJavaType();
            }
        }
        return false;
    }

    private static Attribute<?, ?> findAttribute(ManagedType<?> mt, String name) {
        for (Attribute<?, ?> a : mt.getAttributes()) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    /*
     * Список Java-типов, которые считаем допустимыми для сортировки.
     */
    private static boolean isSortableJavaType(Class<?> t) {
        if (t.isPrimitive()) return true;
        return t == String.class
                || Number.class.isAssignableFrom(t)
                || Boolean.class.isAssignableFrom(t)
                || Enum.class.isAssignableFrom(t)
                || java.util.Date.class.isAssignableFrom(t)
                || java.sql.Date.class.isAssignableFrom(t)
                || java.sql.Timestamp.class.isAssignableFrom(t)
                || java.time.temporal.Temporal.class.isAssignableFrom(t)
                || java.time.Instant.class.isAssignableFrom(t)
                || java.time.LocalDate.class.isAssignableFrom(t)
                || java.time.LocalDateTime.class.isAssignableFrom(t)
                || java.time.OffsetDateTime.class.isAssignableFrom(t)
                || java.time.ZonedDateTime.class.isAssignableFrom(t);
    }
}
