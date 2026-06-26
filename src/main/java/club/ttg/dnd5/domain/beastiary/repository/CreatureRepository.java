package club.ttg.dnd5.domain.beastiary.repository;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.vttg.repository.VttgEntityRef;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
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

    @Query("""
            select c from Creature c
            where (:srdOnly = false or c.srdVersion is not null)
              and (:srdVersion is null or c.srdVersion = :srdVersion)
              and c.isHiddenEntity = false
            order by c.name
            """)
    List<Creature> findAllVisibleForVttgExport(@Param("srdVersion") String srdVersion,
                                               @Param("srdOnly") boolean srdOnly);

    /**
     * Лёгкие ссылки (url + время изменения) видимых существ окна — без гидрации jsonb,
     * для сопоставления с предрассчитанными payload в {@code vttg_export}.
     */
    @Query("""
            select c.url as url, coalesce(c.updatedAt, c.createdAt) as changedAt from Creature c
            where (:srdOnly = false or c.srdVersion is not null)
              and (:srdVersion is null or c.srdVersion = :srdVersion)
              and c.isHiddenEntity = false
              and coalesce(c.updatedAt, c.createdAt) > :since
              and coalesce(c.updatedAt, c.createdAt) <= :until
            """)
    List<VttgEntityRef> findChangedRefsForVttgExport(@Param("srdVersion") String srdVersion,
                                                     @Param("srdOnly") boolean srdOnly,
                                                     @Param("since") Instant since,
                                                     @Param("until") Instant until);

    /** Полные существа по набору url — для пересчёта недостающих payload (fallback экспорта VTTG). */
    @EntityGraph(attributePaths = {"source"})
    @Query("select c from Creature c where c.url in :urls")
    List<Creature> findAllForVttgExportByUrls(@Param("urls") Collection<String> urls);

    /**
     * Число видимых существ, изменённых в окне (since, until] — для индикатора VTTG.
     * Скрытые сущности (мягкое удаление) не учитываются.
     */
    @Query("""
            select count(c) from Creature c
            where (:srdOnly = false or c.srdVersion is not null)
              and (:srdVersion is null or c.srdVersion = :srdVersion)
              and c.isHiddenEntity = false
              and coalesce(c.updatedAt, c.createdAt) > :since
              and coalesce(c.updatedAt, c.createdAt) <= :until
            """)
    long countChangedForVttgExport(@Param("srdVersion") String srdVersion,
                                   @Param("srdOnly") boolean srdOnly,
                                   @Param("since") Instant since,
                                   @Param("until") Instant until);

}

