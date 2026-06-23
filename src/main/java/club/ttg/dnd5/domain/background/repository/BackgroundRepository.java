package club.ttg.dnd5.domain.background.repository;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.vttg.repository.VttgEntityRef;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface BackgroundRepository extends JpaRepository<Background, String>,
        JpaSpecificationExecutor<Background> {
    @Query(value = """
            select b from Background b
            where b.name ilike concat('%', :searchLine, '%')
               or b.english ilike concat('%', :searchLine, '%')
               or b.alternative ilike concat('%', :searchLine, '%')
               or b.name ilike concat('%', :invertedSearchLine, '%')
               or b.english ilike concat('%', :invertedSearchLine, '%')
               or b.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    List<Background> findBySearchLine(String searchLine, String invertedSearchLine, Sort sort);

    /**
     * Переносит ссылки предысторий со старой черты на новую — используется при смене url черты.
     * Новая черта должна уже существовать в БД (иначе нарушится FK fk_background_on_feat).
     */
    @Modifying
    @Query(value = "update background set feat_id = :newFeatUrl where feat_id = :oldFeatUrl",
            nativeQuery = true)
    void repointFeat(@Param("oldFeatUrl") String oldFeatUrl, @Param("newFeatUrl") String newFeatUrl);

    @Query(value = """
        select distinct b.source
        from background b
        where b.source is not null
        order by b.source
        """, nativeQuery = true)
    List<String> findAllUsedSourceCodes();

    @Query(value = """
        select distinct b.srd_version
        from background b
        where b.srd_version is not null
        order by b.srd_version
        """, nativeQuery = true)
    List<String> findDistinctSrdVersions();

    /** Лёгкие ссылки (url + время изменения) видимых предысторий окна — без гидрации jsonb. */
    @Query("""
            select b.url as url, coalesce(b.updatedAt, b.createdAt) as changedAt from Background b
            where (:srdOnly = false or b.srdVersion is not null)
              and (:srdVersion is null or b.srdVersion = :srdVersion)
              and b.isHiddenEntity = false
              and coalesce(b.updatedAt, b.createdAt) > :since
              and coalesce(b.updatedAt, b.createdAt) <= :until
            """)
    List<VttgEntityRef> findChangedRefsForVttgExport(@Param("srdVersion") String srdVersion,
                                                     @Param("srdOnly") boolean srdOnly,
                                                     @Param("since") Instant since,
                                                     @Param("until") Instant until);

    /** Полные предыстории по набору url — для пересчёта недостающих payload (fallback). */
    @EntityGraph(attributePaths = {"source", "feat"})
    @Query("select b from Background b where b.url in :urls")
    List<Background> findAllForVttgExportByUrls(@Param("urls") Collection<String> urls);

    /**
     * Число видимых предысторий, изменённых в окне (since, until] — для индикатора VTTG.
     */
    @Query("""
            select count(b) from Background b
            where (:srdOnly = false or b.srdVersion is not null)
              and (:srdVersion is null or b.srdVersion = :srdVersion)
              and b.isHiddenEntity = false
              and coalesce(b.updatedAt, b.createdAt) > :since
              and coalesce(b.updatedAt, b.createdAt) <= :until
            """)
    long countChangedForVttgExport(@Param("srdVersion") String srdVersion,
                                   @Param("srdOnly") boolean srdOnly,
                                   @Param("since") Instant since,
                                   @Param("until") Instant until);
}
