package club.ttg.dnd5.domain.bastion.player.activity;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerBastionOrderRepository extends JpaRepository<PlayerBastionOrder, UUID> {
    List<PlayerBastionOrder> findAllByBastionIdAndStatus(UUID bastionId, PlayerBastionOrder.OrderStatus status);

    /** Приказы, отданные в этот ход и не отменённые, — для правила «Обслуживать». */
    List<PlayerBastionOrder> findAllByBastionIdAndGivenOnTurnAndStatusNot(
            UUID bastionId, int givenOnTurn, PlayerBastionOrder.OrderStatus status);

    List<PlayerBastionOrder> findAllByBastionIdOrderByCreatedAtDesc(UUID bastionId, Pageable pageable);
}
