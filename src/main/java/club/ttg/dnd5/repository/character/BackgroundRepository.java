package club.ttg.dnd5.repository.character;

import club.ttg.dnd5.model.character.Background;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BackgroundRepository extends JpaRepository<Background, String>,
        JpaSpecificationExecutor<Background> {

}
