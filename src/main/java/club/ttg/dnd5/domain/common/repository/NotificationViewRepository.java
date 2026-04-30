package club.ttg.dnd5.domain.common.repository;

import club.ttg.dnd5.domain.common.model.notification.NotificationView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
