package club.ttg.dnd5.domain.bastion.player.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerBastionEventRepository extends JpaRepository<PlayerBastionEvent, UUID> {
    List<PlayerBastionEvent> findAllByBastionIdOrderByCreatedAtDesc(UUID bastionId, Pageable pageable);

    boolean existsByBastionIdAndTurn(UUID bastionId, int turn);
}
