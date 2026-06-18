package club.ttg.dnd5.domain.magic.repository;

import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.vttg.repository.VttgEntityRef;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface MagicItemRepository extends JpaRepository<MagicItem, String> {
    @Query(value = """
            select mi from MagicItem mi
            where mi.name ilike concat('%', :searchLine, '%')
               or mi.english ilike concat('%', :searchLine, '%')
               or mi.alternative ilike concat('%', :searchLine, '%')
               or mi.name ilike concat('%', :invertedSearchLine, '%')
               or mi.english ilike concat('%', :invertedSearchLine, '%')
               or mi.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    Collection<MagicItem> findBySearchLine(String searchLine, String invertedSearchLine);

    @Query(value = """
        select distinct mi.source
        from magic_item mi
        where mi.source is not null
        order by mi.source
        """, nativeQuery = true)
    List<String> findAllUsedSourceCodes();

    @Query(value = """
        select distinct mi.srd_version
        from magic_item mi
        where mi.srd_version is not null
        order by mi.srd_version
        """, nativeQuery = true)
    List<String> findDistinctSrdVersions();

    /**
     * Все видимые магические предметы (опц. фильтр по версии SRD) — для полной выгрузки модуля VTTG.
     */
    @EntityGraph(attributePaths = {"source"})
    @Query("""
            select mi from MagicItem mi
            where (:srdVersion is null or mi.srdVersion = :srdVersion)
              and mi.isHiddenEntity = false
            order by mi.name
            """)
    List<MagicItem> findAllVisibleForVttgExport(@Param("srdVersion") String srdVersion);

    /**
     * Видимые магические предметы, изменённые в окне (since, until] — для upserts дельты VTTG.
     * Сортировка по времени изменения выполняется на стороне приложения.
     */
    @EntityGraph(attributePaths = {"source"})
    @Query("""
            select mi from MagicItem mi
            where (:srdVersion is null or mi.srdVersion = :srdVersion)
              and mi.isHiddenEntity = false
              and coalesce(mi.updatedAt, mi.createdAt) > :since
              and coalesce(mi.updatedAt, mi.createdAt) <= :until
            """)
    List<MagicItem> findChangedForVttgExport(@Param("srdVersion") String srdVersion,
                                             @Param("since") Instant since,
                                             @Param("until") Instant until);

    /** Лёгкие ссылки (url + время изменения) видимых магических предметов окна — без гидрации jsonb. */
    @Query("""
            select mi.url as url, coalesce(mi.updatedAt, mi.createdAt) as changedAt from MagicItem mi
            where (:srdVersion is null or mi.srdVersion = :srdVersion)
              and mi.isHiddenEntity = false
              and coalesce(mi.updatedAt, mi.createdAt) > :since
              and coalesce(mi.updatedAt, mi.createdAt) <= :until
            """)
    List<VttgEntityRef> findChangedRefsForVttgExport(@Param("srdVersion") String srdVersion,
                                                     @Param("since") Instant since,
                                                     @Param("until") Instant until);

    /** Полные магические предметы по набору url — для пересчёта недостающих payload (fallback). */
    @EntityGraph(attributePaths = {"source"})
    @Query("select mi from MagicItem mi where mi.url in :urls")
    List<MagicItem> findAllForVttgExportByUrls(@Param("urls") Collection<String> urls);

    /**
     * Число видимых магических предметов, изменённых в окне (since, until] — для индикатора VTTG.
     * Скрытые сущности (мягкое удаление) не учитываются.
     */
    @Query("""
            select count(mi) from MagicItem mi
            where (:srdVersion is null or mi.srdVersion = :srdVersion)
              and mi.isHiddenEntity = false
              and coalesce(mi.updatedAt, mi.createdAt) > :since
              and coalesce(mi.updatedAt, mi.createdAt) <= :until
            """)
    long countChangedForVttgExport(@Param("srdVersion") String srdVersion,
                                   @Param("since") Instant since,
                                   @Param("until") Instant until);
}
