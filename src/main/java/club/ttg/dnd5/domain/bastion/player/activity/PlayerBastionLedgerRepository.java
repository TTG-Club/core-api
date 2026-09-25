package club.ttg.dnd5.domain.bastion.player.activity;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerBastionLedgerRepository extends JpaRepository<PlayerBastionLedgerEntry, UUID> {
    List<PlayerBastionLedgerEntry> findAllByBastionIdOrderByCreatedAtDesc(UUID bastionId, Pageable pageable);
}
