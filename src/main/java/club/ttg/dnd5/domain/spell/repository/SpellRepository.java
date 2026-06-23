package club.ttg.dnd5.domain.spell.repository;

import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.vttg.repository.VttgEntityRef;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SpellRepository extends JpaRepository<Spell, String> {
    @EntityGraph(attributePaths = {
            "source",
            "classAffiliation", "classAffiliation.source",
            "subclassAffiliation", "subclassAffiliation.source",
            "speciesAffiliation", "speciesAffiliation.source",
            "lineagesAffiliation", "lineagesAffiliation.source",
            "featAffiliation", "featAffiliation.source"
    })
    @Query("select s from Spell s where s.url = :url")
    Optional<Spell> findDetailedByUrl(String url);

    @EntityGraph(attributePaths = {
            "source",
            "classAffiliation",
            "subclassAffiliation",
            "speciesAffiliation",
            "lineagesAffiliation",
            "featAffiliation"
    })
    @Query("select s from Spell s where s.url = :url")
    Optional<Spell> findFormByUrl(String url);

    @Query(value = """
            select s from Spell s
            where s.name ilike concat('%', :searchLine, '%')
               or s.english ilike concat('%', :searchLine, '%')
               or s.alternative ilike concat('%', :searchLine, '%')
               or s.name ilike concat('%', :invertedSearchLine, '%')
               or s.english ilike concat('%', :invertedSearchLine, '%')
               or s.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    List<Spell> findBySearchLine(String searchLine, String invertedSearchLine, Sort sort);

    @Query(value = """
            select distinct s.source
            from spell s
            where s.source is not null
            order by s.source
            """, nativeQuery = true)
    List<String> findAllUsedSourceCodes();

    @Query(value = """
            select distinct
                case
                    when elem->>'value' is null then elem->>'unit'
                    else concat(elem->>'value', '_', elem->>'unit')
                end
            from spell s
                cross join jsonb_array_elements(s.range) as elem
            where s.is_hidden_entity = false
                and s.source in (:sourceCodes)
                and elem->>'unit' is not null
            """, nativeQuery = true)
    List<String> findAllUsedDistanceIds(@Param("sourceCodes") Set<String> sourceCodes);

    @EntityGraph(attributePaths = {"source"})
    @Query("select s from Spell s where s.url = :url")
    Optional<Spell> findForUpdateByUrl(String url);

    @Query(value = """
        select distinct s.srd_version
        from spell s
        where s.srd_version is not null
        order by s.srd_version
        """, nativeQuery = true)
    List<String> findDistinctSrdVersions();

    @EntityGraph(attributePaths = {
            "source",
            "classAffiliation"
    })
    @Query("""
            select distinct s from Spell s
            where (:srdVersion is null or s.srdVersion = :srdVersion)
              and s.isHiddenEntity = false
            order by s.level, s.name
            """)
    List<Spell> findAllVisibleForVttgExport(@Param("srdVersion") String srdVersion);

    /**
     * Лёгкие ссылки (url + время изменения) видимых заклинаний окна — без гидрации jsonb,
     * для сопоставления с предрассчитанными payload в {@code vttg_export}.
     */
    @Query("""
            select s.url as url, coalesce(s.updatedAt, s.createdAt) as changedAt from Spell s
            where (:srdVersion is null or s.srdVersion = :srdVersion)
              and s.isHiddenEntity = false
              and coalesce(s.updatedAt, s.createdAt) > :since
              and coalesce(s.updatedAt, s.createdAt) <= :until
            """)
    List<VttgEntityRef> findChangedRefsForVttgExport(@Param("srdVersion") String srdVersion,
                                                     @Param("since") Instant since,
                                                     @Param("until") Instant until);

    /** Полные заклинания по набору url — для пересчёта недостающих payload (fallback экспорта VTTG). */
    @EntityGraph(attributePaths = {"source", "classAffiliation"})
    @Query("select distinct s from Spell s where s.url in :urls")
    List<Spell> findAllForVttgExportByUrls(@Param("urls") Collection<String> urls);

    /**
     * Число видимых заклинаний, изменённых в окне (since, until] — для индикатора VTTG.
     * Скрытые сущности (мягкое удаление) не учитываются.
     */
    @Query("""
            select count(s) from Spell s
            where (:srdVersion is null or s.srdVersion = :srdVersion)
              and s.isHiddenEntity = false
              and coalesce(s.updatedAt, s.createdAt) > :since
              and coalesce(s.updatedAt, s.createdAt) <= :until
            """)
    long countChangedForVttgExport(@Param("srdVersion") String srdVersion,
                                   @Param("since") Instant since,
                                   @Param("until") Instant until);

}
