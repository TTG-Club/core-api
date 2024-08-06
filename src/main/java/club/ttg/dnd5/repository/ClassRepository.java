package club.ttg.dnd5.repository;

import club.ttg.dnd5.model.character.ClassCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassRepository extends JpaRepository<ClassCharacter, String> {
}
