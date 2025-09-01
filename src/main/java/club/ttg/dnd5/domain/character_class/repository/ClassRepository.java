package club.ttg.dnd5.domain.character_class.repository;

import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClassRepository extends JpaRepository<CharacterClass, String> {

    @Query("""
                    select cc from CharacterClass cc
                    join fetch cc.subclasses
                     where cc.parent is null
            """)
    List<CharacterClass> findAllClassesFetchSubclasses();

}
