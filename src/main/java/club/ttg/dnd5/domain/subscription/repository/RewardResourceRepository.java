package club.ttg.dnd5.domain.subscription.repository;

import club.ttg.dnd5.domain.subscription.model.RewardPerk;
import club.ttg.dnd5.domain.subscription.model.RewardResource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardResourceRepository extends JpaRepository<RewardResource, RewardPerk> {
}
