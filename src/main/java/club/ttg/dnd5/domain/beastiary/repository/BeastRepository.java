package club.ttg.dnd5.domain.beastiary.repository;

import club.ttg.dnd5.model.bestiary.Beast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BeastRepository  extends JpaRepository<Beast, String> {
}
