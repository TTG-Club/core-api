package club.ttg.dnd5.domain.background.repository;

import club.ttg.dnd5.domain.background.model.Background;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BackgroundRepository extends JpaRepository<Background, String>,
        JpaSpecificationExecutor<Background> {
}
