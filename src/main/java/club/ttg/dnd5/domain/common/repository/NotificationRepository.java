package club.ttg.dnd5.domain.common.repository;

import club.ttg.dnd5.domain.common.model.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  @Query("""
      SELECT count(n) \
      FROM Notification n
      where n.disabled = false
        AND n.persona.disabled = false
        AND (n.after is null or n.after <= :now)
        AND (n.before is null or n.before >= :now)
        AND (n.view is null or n.view > 0)
      """)
  long countEligible(@Param("now") LocalDateTime now);

  @Query("""
      SELECT n
      FROM Notification n
      WHERE n.disabled = false
        AND n.persona.disabled = false
        AND (n.after is null or n.after <= :now)
        AND (n.before is null or n.before >= :now)
        AND (n.view is null or n.view > 0)
      ORDER BY n.id
      """)
  Page<Notification> findEligible(@Param("now") LocalDateTime now, Pageable pageable);

  @Query("""
      SELECT n
      FROM Notification n
      WHERE n.disabled = false
        AND n.persona.disabled = false
        AND (n.after is null or n.after <= :now)
        AND (n.before is null or n.before >= :now)
        AND (n.view is null or n.view > 0)
      ORDER BY n.id
      """)
  List<Notification> findEligible(@Param("now") LocalDateTime now);
}
