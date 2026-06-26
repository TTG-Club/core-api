package club.ttg.dnd5.domain.subscription.repository;

import club.ttg.dnd5.domain.subscription.model.RewardPerk;
import club.ttg.dnd5.domain.subscription.model.UserReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRewardRepository extends JpaRepository<UserReward, UUID> {
    List<UserReward> findByUsernameOrderByGrantedAtDesc(String username);

    boolean existsByUsernameAndPerk(String username, RewardPerk perk);

    List<UserReward> findByPerkOrderByGrantedAtAsc(RewardPerk perk);
}
