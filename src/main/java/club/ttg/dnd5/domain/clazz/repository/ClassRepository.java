package club.ttg.dnd5.domain.clazz.repository;

import club.ttg.dnd5.domain.clazz.model.ClassCharacter;
import club.ttg.dnd5.domain.clazz.model.ClassFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<ClassCharacter, String>,
        JpaSpecificationExecutor<ClassCharacter> {
    @Query("SELECT class FROM ClassCharacter class WHERE class.parent IS NULL")
    Collection<ClassCharacter> findAllClasses();

    @Query("SELECT class FROM ClassCharacter class WHERE class.parent.url = ?1")
    Collection<ClassCharacter> findAllSubclasses(String url);

    @Query("""
    SELECT cf FROM ClassFeature cf
    JOIN ClassFeatureRelationship cfr ON cf.url = cfr.id.featureUrl.url
    WHERE cfr.id.classUrl.url = :classUrl
    ORDER BY cfr.level ASC
    """)
    List<ClassFeature> findFeaturesByClassUrl(@Param("classUrl") String classUrl);
}
