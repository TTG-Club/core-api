package club.ttg.dnd5.domain.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Отображаемое имя пользователя — то, что показывается на сайте вместо логина
 * (в профиле, комментариях, рейтинге охотников за багами).
 *
 * Владелец данных — core-api (сайтовый бэкенд). Ключ {@code userId} совпадает с
 * {@code sub} JWT (он же {@code authorId} в сервисе комментариев), поэтому смену
 * имени можно синхронно пробросить в другие сервисы. {@code username} хранится,
 * чтобы резолвить «логин → имя» там, где известен только логин (таблица охотников).
 *
 * Строка создаётся лениво при первом обращении: core-api сам пользователей не
 * заводит, поэтому запись появляется при первом чтении/смене имени.
 */
@Getter
@Setter
@Entity
@Table(name = "user_display_name")
public class UserDisplayName {
    /** UUID пользователя из токена ({@code sub}). Присваивается вручную, не генерируется. */
    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
