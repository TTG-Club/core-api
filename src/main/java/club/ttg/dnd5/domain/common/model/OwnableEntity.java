package club.ttg.dnd5.domain.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Базовый класс для контента, который может быть как официальным, так и пользовательским (homebrew).
 * <p>
 * Расширяет {@link NamedEntity}, добавляя владельца и уровень видимости. Официальный и
 * пользовательский контент живут в одной таблице — «не смешиваются» они на уровне запросов
 * (см. предикат видимости), а не хранилища. Это позволяет не дублировать таблицы, код и связи
 * (аффилиации homebrew-контента к официальному работают через те же FK).
 * <p>
 * Правило разграничения:
 * <ul>
 *   <li>{@code ownerId == null} — официальный контент (доступен всем, {@code visibility} игнорируется);</li>
 *   <li>{@code ownerId != null} — homebrew, доступ определяется {@link #visibility}.</li>
 * </ul>
 * <b>Источник правды для «официальное vs homebrew» и авторизации — именно {@code ownerId}</b>,
 * а не форма url (url-суффикс/префикс — лишь косметика и гарантия уникальности PK).
 */
@Getter
@Setter
@MappedSuperclass
public abstract class OwnableEntity extends NamedEntity {

    /**
     * Владелец homebrew-контента (UUID пользователя). {@code null} — официальный контент.
     * Хранится как идентификатор, а не как {@code @ManyToOne User}, чтобы не тянуть связь
     * в каждый запрос контента и не связывать граф загрузки с доменом пользователей.
     */
    @Column(name = "owner_id")
    private UUID ownerId;

    /**
     * Видимость homebrew-контента. Для официального контента ({@code ownerId == null}) не используется;
     * по умолчанию новых пользовательских записей — {@link Visibility#PRIVATE}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    private Visibility visibility;

    /** {@code true}, если у контента есть владелец (это homebrew, а не официальная запись). */
    @Transient
    public boolean isHomebrew() {
        return ownerId != null;
    }
}
