package club.ttg.dnd5.domain.feat.repository;

import club.ttg.dnd5.domain.feat.model.Feat;
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
public interface FeatRepository extends JpaRepository<Feat, String> {
    @Query(value = """
            select f from Feat f
            where f.name ilike concat('%', :searchLine, '%')
               or f.english ilike concat('%', :searchLine, '%')
               or f.alternative ilike concat('%', :searchLine, '%')
               or f.name ilike concat('%', :invertedSearchLine, '%')
               or f.english ilike concat('%', :invertedSearchLine, '%')
               or f.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    Collection<Feat> findBySearchLine(String searchLine, String invertedSearchLine, Sort defaultSort);

    @Query(value = """
        select distinct f.source
        from feat f
        where f.source is not null
        order by f.source
        """, nativeQuery = true)
    List<String> findAllUsedSourceCodes();

    @Query(value = """
        select distinct f.srd_version
        from feat f
        where f.srd_version is not null
        order by f.srd_version
        """, nativeQuery = true)
    List<String> findDistinctSrdVersions();

    /**
     * Видимые черты, изменённые в окне (since, until] — для upserts дельты VTTG.
     * Сортировка по времени изменения выполняется на стороне приложения.
     */
    @EntityGraph(attributePaths = {"source"})
    @Query("""
            select f from Feat f
            where (:srdVersion is null or f.srdVersion = :srdVersion)
              and f.isHiddenEntity = false
              and coalesce(f.updatedAt, f.createdAt) > :since
              and coalesce(f.updatedAt, f.createdAt) <= :until
            """)
    List<Feat> findChangedForVttgExport(@Param("srdVersion") String srdVersion,
                                        @Param("since") Instant since,
                                        @Param("until") Instant until);

    /**
     * Число видимых черт, изменённых в окне (since, until] — для индикатора VTTG.
     */
    @Query("""
            select count(f) from Feat f
            where (:srdVersion is null or f.srdVersion = :srdVersion)
              and f.isHiddenEntity = false
              and coalesce(f.updatedAt, f.createdAt) > :since
              and coalesce(f.updatedAt, f.createdAt) <= :until
            """)
    long countChangedForVttgExport(@Param("srdVersion") String srdVersion,
                                   @Param("since") Instant since,
                                   @Param("until") Instant until);
}
