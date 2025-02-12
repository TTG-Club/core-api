package club.ttg.dnd5.repository;

import club.ttg.dnd5.model.spell.Spell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpellRepository extends JpaRepository<Spell, String> {
}
