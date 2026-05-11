package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.domain.common.repository.NotificationViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Периодически удаляет устаревшие записи из таблицы notification_view,
 * чтобы она не разрасталась бесконечно.
 * <p>
 * Хранит записи за последние N дней (настраивается через notification.view-retention-days).
 * Бизнес-логика использует только данные за текущие сутки (ротация рекламы)
 * и последний просмотр пользователя, поэтому 2 дня — безопасный запас по умолчанию.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationViewCleanupScheduler {

    private final NotificationViewRepository notificationViewRepository;

    @Value("${notification.view-retention-days:2}")
    private int retentionDays;

    /**
     * Запускается ежедневно в 04:00 по серверному времени.
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanupOldViews() {
        var cutoff = LocalDateTime.now().minusDays(retentionDays);
        var deleted = notificationViewRepository.deleteByViewedAtBefore(cutoff);
        if (deleted > 0) {
            log.info("Очистка notification_view: удалено {} записей старше {}", deleted, cutoff);
        }
    }
}
