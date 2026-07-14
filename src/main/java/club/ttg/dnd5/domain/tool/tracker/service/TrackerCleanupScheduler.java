package club.ttg.dnd5.domain.tool.tracker.service;

import club.ttg.dnd5.config.properties.TrackerProperties;
import club.ttg.dnd5.domain.tool.tracker.repository.InitiativeTrackerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Периодически удаляет анонимные трекеры инициативы без активности.
 * <p>
 * У анонимного трекера нет владельца: если клиент потерял ключ (очистил localStorage),
 * трекер больше никому не доступен и копится мусором. Признак активности — updated_at
 * строки трекера; операции только с участниками дополнительно «трогают» трекер
 * (см. {@link InitiativeTrackerRepository#touch}). Трекеры владельцев не удаляются —
 * они видны в истории пользователя.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TrackerCleanupScheduler {

    private final InitiativeTrackerRepository trackerRepository;
    private final TrackerProperties properties;

    @Transactional
    @Scheduled(fixedDelayString = "${tracker.cleanup-interval:PT1H}")
    public void cleanupStaleAnonymous() {
        int removed = trackerRepository.deleteStaleAnonymous(Instant.now().minus(properties.getAnonymousTtl()));
        if (removed > 0) {
            log.info("Удалено анонимных трекеров инициативы без активности: {}", removed);
        }
    }
}
