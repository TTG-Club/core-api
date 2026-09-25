package club.ttg.dnd5.domain.bastion.player.activity;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerBastionTurnRepository extends JpaRepository<PlayerBastionTurn, UUID> {
    List<PlayerBastionTurn> findAllByBastionIdOrderByNumberDesc(UUID bastionId, Pageable pageable);
}
