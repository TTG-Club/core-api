package club.ttg.dnd5.domain.glossary.repository;

import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.vttg.repository.VttgEntityRef;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface GlossaryRepository extends JpaRepository<Glossary, String> {
    @Query(value = """
            select g from Glossary g
            where g.name ilike concat('%', :searchLine, '%')
               or g.english ilike concat('%', :searchLine, '%')
               or g.alternative ilike concat('%', :searchLine, '%')
               or g.english ilike concat('%', :invertedSearchLine, '%')
               or g.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    List<Glossary> findBySearchLine(String searchLine, String invertedSearchLine, Sort sort);

    @Query(value = """
        select distinct g.source
        from glossary g
        where g.source is not null
        order by g.source
        """, nativeQuery = true)
    List<String> findAllUsedSourceCodes();

    /** Лёгкие ссылки (url + время изменения) видимых записей глоссария окна — без гидрации описаний. */
    @Query("""
            select g.url as url, coalesce(g.updatedAt, g.createdAt) as changedAt from Glossary g
            where (:srdOnly = false or g.srdVersion is not null)
              and (:srdVersion is null or g.srdVersion = :srdVersion)
              and g.isHiddenEntity = false
              and coalesce(g.updatedAt, g.createdAt) > :since
              and coalesce(g.updatedAt, g.createdAt) <= :until
            """)
    List<VttgEntityRef> findChangedRefsForVttgExport(@Param("srdVersion") String srdVersion,
                                                     @Param("srdOnly") boolean srdOnly,
                                                     @Param("since") Instant since,
                                                     @Param("until") Instant until);

    /** Полные записи глоссария по набору url — для пересчёта недостающих payload (fallback). */
    @EntityGraph(attributePaths = "source")
    @Query("select g from Glossary g where g.url in :urls")
    List<Glossary> findAllForVttgExportByUrls(@Param("urls") Collection<String> urls);

    /** Число видимых записей глоссария, изменённых в окне (since, until] — для индикатора VTTG. */
    @Query("""
            select count(g) from Glossary g
            where (:srdOnly = false or g.srdVersion is not null)
              and (:srdVersion is null or g.srdVersion = :srdVersion)
              and g.isHiddenEntity = false
              and coalesce(g.updatedAt, g.createdAt) > :since
              and coalesce(g.updatedAt, g.createdAt) <= :until
            """)
    long countChangedForVttgExport(@Param("srdVersion") String srdVersion,
                                   @Param("srdOnly") boolean srdOnly,
                                   @Param("since") Instant since,
                                   @Param("until") Instant until);
}
