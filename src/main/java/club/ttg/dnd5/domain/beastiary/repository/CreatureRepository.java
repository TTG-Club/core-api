package club.ttg.dnd5.domain.beastiary.repository;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreatureRepository extends JpaRepository<Creature, String> {
    @Query(value = """
            select b from Creature b
            where b.name ilike concat('%', :searchLine, '%')
               or b.english ilike concat('%', :searchLine, '%')
               or b.alternative ilike concat('%', :searchLine, '%')
               or b.name ilike concat('%', :invertedSearchLine, '%')
               or b.english ilike concat('%', :invertedSearchLine, '%')
               or b.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    List<Creature> findBySearchLine(String searchLine, String invertedSearchLine, Sort sort);

    @Query(value = """
        select distinct s.source
        from bestiary s
        where s.source is not null
        order by s.source
        """, nativeQuery = true)
    List<String> findAllUsedSourceCodes();

    /**
     * Уникальные теги (types->>'text') напрямую из JSONB, без загрузки сущностей.
     */
    @Query(value = """
        SELECT DISTINCT LOWER(b.types->>'text')
        FROM bestiary b
        WHERE b.types->>'text' IS NOT NULL
          AND b.types->>'text' != ''
        ORDER BY 1
        """, nativeQuery = true)
    List<String> findDistinctTags();

    @Query(value = """
        select distinct b.srd_version
        from bestiary b
        where b.srd_version is not null
        order by b.srd_version
        """, nativeQuery = true)
    List<String> findDistinctSrdVersions();

}

