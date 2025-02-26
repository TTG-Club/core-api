package club.ttg.dnd5.domain.beastiary.repository;

import club.ttg.dnd5.domain.beastiary.model.Beast;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeastRepository  extends JpaRepository<Beast, String> {
}
