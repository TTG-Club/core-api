package club.ttg.dnd5.domain.achievement.repository;

import club.ttg.dnd5.domain.achievement.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    List<UserAchievement> findByUsernameOrderByGrantedAtDesc(String username);

    boolean existsByUsernameAndAchievementCode(String username, String achievementCode);
}
