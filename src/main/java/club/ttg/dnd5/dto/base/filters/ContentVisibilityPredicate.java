package club.ttg.dnd5.dto.base.filters;

import club.ttg.dnd5.domain.common.model.Visibility;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * Переиспользуемые QueryDSL-предикаты видимости контента ({@code owner_id} + {@code visibility}).
 * <p>
 * Entity-agnostic: пути строятся по именам свойств {@code OwnableEntity} через {@link Expressions}
 * (как {@code SpellPredicateBuilder.SCHOOL_PATH}), поэтому один набор предикатов подходит любой
 * контентной сущности, наследующей {@code OwnableEntity}. Колонки {@code owner_id}/{@code visibility}
 * одинаковы во всех таблицах контента.
 * <p>
 * Это и есть механизм «не смешивать» официальный и homebrew-контент: официальная выдача остаётся
 * прежней ({@link #official()} = {@code owner_id IS NULL}), homebrew подмешивается только явно.
 */
@UtilityClass
public class ContentVisibilityPredicate {

    private final ComparablePath<UUID> OWNER_ID = Expressions.comparablePath(UUID.class, "ownerId");
    private final StringPath VISIBILITY = Expressions.stringPath("visibility");

    /** Только официальный контент (без владельца). Поведение существующих списков. */
    public BooleanExpression official() {
        return OWNER_ID.isNull();
    }

    /** Публичный homebrew: есть владелец и видимость PUBLIC. */
    public BooleanExpression publicHomebrew() {
        return OWNER_ID.isNotNull().and(VISIBILITY.eq(Visibility.PUBLIC.name()));
    }

    /** Контент конкретного владельца (все его записи, независимо от видимости). */
    public BooleanExpression ownedBy(UUID ownerId) {
        return OWNER_ID.eq(ownerId);
    }

    /**
     * Что показывать в общих списках: официальное + публичный homebrew, плюс собственный контент
     * пользователя (если он аутентифицирован).
     *
     * @param currentUserId uuid текущего пользователя либо {@code null} для анонима
     */
    public BooleanExpression listableFor(UUID currentUserId) {
        BooleanExpression base = official().or(publicHomebrew());
        return currentUserId == null ? base : base.or(ownedBy(currentUserId));
    }

    /**
     * Можно ли отдать запись по прямой ссылке (GET по url): официальное, либо не-приватный homebrew,
     * либо собственный контент пользователя. Приватный чужой контент недоступен.
     *
     * @param currentUserId uuid текущего пользователя либо {@code null} для анонима
     */
    public BooleanExpression readableByLinkFor(UUID currentUserId) {
        BooleanExpression base = official().or(VISIBILITY.ne(Visibility.PRIVATE.name()));
        return currentUserId == null ? base : base.or(ownedBy(currentUserId));
    }
}
