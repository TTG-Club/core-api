package club.ttg.dnd5.domain.achievement.repository;

import club.ttg.dnd5.domain.achievement.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, String> {
    List<Achievement> findByTriggerKey(String triggerKey);

    List<Achievement> findByHiddenFalseOrderByTitleAsc();
}
