package club.ttg.dnd5.domain.bastion.player.plan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayerBastionPlanRepository extends JpaRepository<PlayerBastionPlan, UUID> {
}
