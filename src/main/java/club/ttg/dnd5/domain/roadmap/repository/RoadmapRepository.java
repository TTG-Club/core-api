package club.ttg.dnd5.domain.roadmap.repository;

import club.ttg.dnd5.domain.roadmap.model.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, String> {
    List<Roadmap> findAllByVisible(boolean visible);
}
