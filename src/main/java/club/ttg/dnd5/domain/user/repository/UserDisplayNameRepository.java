package club.ttg.dnd5.domain.user.repository;

import club.ttg.dnd5.domain.user.model.UserDisplayName;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserDisplayNameRepository extends JpaRepository<UserDisplayName, UUID> {

    /** Занято ли имя (регистронезависимо) кем угодно — для генерации уникального дефолта. */
    boolean existsByDisplayNameIgnoreCase(String displayName);

    /** Занято ли имя (регистронезависимо) другим пользователем — для проверки при смене. */
    boolean existsByDisplayNameIgnoreCaseAndUserIdNot(String displayName, UUID userId);

    /**
     * Резолв «логины → отображаемые имена» для публичных рейтингов (таблица охотников
     * за багами). Логины передаются уже в нижнем регистре.
     */
    @Query("SELECT u FROM UserDisplayName u WHERE lower(u.username) IN :usernames")
    List<UserDisplayName> findAllByUsernameLowerIn(@Param("usernames") Collection<String> usernames);

    /**
     * Поиск по отображаемому имени (подстрока, регистронезависимо) — для подсказок
     * в админке. Размер выборки ограничивает вызывающий через {@link Pageable}.
     */
    @Query("SELECT u FROM UserDisplayName u WHERE lower(u.displayName) LIKE lower(concat('%', :query, '%')) "
            + "ORDER BY u.displayName")
    List<UserDisplayName> searchByDisplayName(@Param("query") String query, Pageable pageable);
}
