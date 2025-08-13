package club.ttg.dnd5.domain.common.repository;

import club.ttg.dnd5.domain.common.model.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
