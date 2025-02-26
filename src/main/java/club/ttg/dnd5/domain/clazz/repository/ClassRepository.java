package club.ttg.dnd5.domain.clazz.repository;

import club.ttg.dnd5.domain.clazz.model.ClassCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ClassRepository extends JpaRepository<ClassCharacter, String>,
        JpaSpecificationExecutor<ClassCharacter> {
    @Query("SELECT class FROM ClassCharacter class WHERE class.parent IS NULL")
    Collection<ClassCharacter> findAllClasses();

    @Query("SELECT class FROM ClassCharacter class WHERE class.parent.url = ?1")
    Collection<ClassCharacter> findAllSubclasses(String url);
}
