package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.source.event.SourceAcronymChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Реакция выгрузки VTTG на смену акронима источника.
 *
 * <p>Ключ источника в выгрузке при смене акронима не меняется (см. {@link VttgSourceKeys}),
 * поэтому записи компендиума остаются как были и версию поднимать не нужно. Меняется лишь
 * аббревиатура в словаре источников — он лежит в кэше полного дампа, который и сбрасывается.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgSourceRenameListener {
    private final VttgCompendiumRebuildService rebuildService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAcronymChanged(SourceAcronymChangedEvent event) {
        rebuildService.evictFullExport();
    }
}
