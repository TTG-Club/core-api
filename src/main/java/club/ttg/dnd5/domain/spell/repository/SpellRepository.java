package club.ttg.dnd5.domain.spell.repository;

import club.ttg.dnd5.domain.spell.model.Spell;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpellRepository extends JpaRepository<Spell, String> {

}
