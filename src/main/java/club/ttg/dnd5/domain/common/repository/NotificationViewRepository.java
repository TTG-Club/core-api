package club.ttg.dnd5.domain.common.repository;

import club.ttg.dnd5.domain.common.model.notification.NotificationView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public interface NotificationViewRepository extends JpaRepository<NotificationView, Long> {
    Optional<NotificationView> findFirstByUsernameOrderByViewedAtDescIdDesc(String username);

    @Query("""
        SELECT view.notification.id
        FROM NotificationView view
        WHERE view.username = :username
          AND view.notification.id IN :notificationIds
        """)
    Set<Long> findViewedNotificationIds(
            @Param("username") String username,
            @Param("notificationIds") Collection<Long> notificationIds);

    /**
     * Проверяет, было ли хоть одно просмотренное уведомление начиная с указанного момента (начало суток).
     */
    @Query("""
        SELECT COUNT(v) > 0
        FROM NotificationView v
        WHERE v.username = :username
          AND v.viewedAt >= :startOfDay
        """)
    boolean existsViewedSinceByUsername(
            @Param("username") String username,
            @Param("startOfDay") LocalDateTime startOfDay);

    /**
     * Возвращает последний просмотр рекламной нотификации для данного пользователя.
     */
    @Query("""
        SELECT v
        FROM NotificationView v
        WHERE v.username = :username
          AND v.notification.type = club.ttg.dnd5.domain.common.model.notification.NotificationType.ADVERTISING
        ORDER BY v.viewedAt DESC, v.id DESC
        LIMIT 1
        """)
    Optional<NotificationView> findLastAdvertisingView(@Param("username") String username);

    /**
     * Возвращает ID рекламных нотификаций, просмотренных пользователем начиная с указанного момента.
     */
    @Query("""
        SELECT v.notification.id
        FROM NotificationView v
        WHERE v.username = :username
          AND v.notification.type = club.ttg.dnd5.domain.common.model.notification.NotificationType.ADVERTISING
          AND v.viewedAt >= :since
        """)
    Set<Long> findViewedAdvertisingIdsSince(
            @Param("username") String username,
            @Param("since") LocalDateTime since);

    /**
     * Удаляет все записи просмотров, созданные ранее указанной даты.
     */
    @Modifying
    @Query("DELETE FROM NotificationView v WHERE v.viewedAt < :before")
    int deleteByViewedAtBefore(@Param("before") LocalDateTime before);
}
