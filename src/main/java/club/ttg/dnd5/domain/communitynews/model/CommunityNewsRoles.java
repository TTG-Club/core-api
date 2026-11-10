package club.ttg.dnd5.domain.communitynews.model;

import org.springframework.security.access.annotation.Secured;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Кто правит «Новости сообщества». Список ролей — только здесь: когда инструмент отдадут отдельной
 * роли, её достаточно дописать в {@link #EDITORS} (и в {@link Editors}).
 */
public final class CommunityNewsRoles {

    public static final String ADMIN = "ADMIN";
    public static final String MODERATOR = "MODERATOR";

    /** Роли редакторов новостей сообщества. */
    public static final String[] EDITORS = {ADMIN, MODERATOR};

    private CommunityNewsRoles() {
    }

    /**
     * Доступ только редакторам новостей сообщества. Мета-аннотация над {@link Secured}: значение
     * аннотации обязано быть литералом, поэтому роли перечислены здесь, а методы помечаются {@code @Editors}.
     */
    @Documented
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Secured({ADMIN, MODERATOR})
    public @interface Editors {
    }
}
