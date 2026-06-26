package club.ttg.dnd5.domain.species.repository;

import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.vttg.repository.VttgEntityRef;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface SpeciesRepository extends JpaRepository<Species, String> {
    Collection<Species> findByParent(Species parent);

    @Query(value = """
        select s from Species s
        where s.parent is not null
        order by s.parent.name, s.name
        """)
    Collection<Species> findAllByParentIsNotNull();

    @Query(value = """
        select distinct s.source
        from species s
        where s.source is not null
        order by s.source
        """, nativeQuery = true)
    List<String> findAllUsedSourceCodes();

    @Query(value = """
        select distinct s.srd_version
        from species s
        where s.srd_version is not null
        order by s.srd_version
        """, nativeQuery = true)
    List<String> findDistinctSrdVersions();

    /**
     * Лёгкие ссылки (url + время изменения) видов окна — без гидрации jsonb. Возвращает видимые
     * верхнеуровневые виды (без родителя), изменённые сами либо через своё происхождение (подзапрос
     * {@code exists}), иначе правка дочернего вида не доехала бы до клиента. Происхождения отдельными
     * записями не выгружаются — они сворачиваются в {@code choices} умения родителя. Время —
     * собственное время вида.
     */
    @Query("""
            select s.url as url, coalesce(s.updatedAt, s.createdAt) as changedAt from Species s
            where (:srdOnly = false or s.srdVersion is not null)
              and (:srdVersion is null or s.srdVersion = :srdVersion)
              and s.isHiddenEntity = false
              and s.parent is null
              and (
                    (coalesce(s.updatedAt, s.createdAt) > :since and coalesce(s.updatedAt, s.createdAt) <= :until)
                 or exists (
                        select 1 from Species l
                        where l.parent = s
                          and l.isHiddenEntity = false
                          and coalesce(l.updatedAt, l.createdAt) > :since
                          and coalesce(l.updatedAt, l.createdAt) <= :until
                    )
              )
            """)
    List<VttgEntityRef> findChangedRefsForVttgExport(@Param("srdVersion") String srdVersion,
                                                     @Param("srdOnly") boolean srdOnly,
                                                     @Param("since") Instant since,
                                                     @Param("until") Instant until);

    /** Полные виды по набору url — для пересчёта недостающих payload (fallback). */
    @EntityGraph(attributePaths = {"source", "lineages"})
    @Query("select distinct s from Species s where s.url in :urls")
    List<Species> findAllForVttgExportByUrls(@Param("urls") Collection<String> urls);

    /**
     * Максимум времени изменения видимых видов (включая происхождения) — «отметка зависимостей»:
     * payload родителя сворачивает данные происхождений, правка происхождения должна его инвалидировать.
     */
    @Query("select max(coalesce(s.updatedAt, s.createdAt)) from Species s where s.isHiddenEntity = false")
    Instant maxChangedAtForVttgExport();

    /**
     * Число видимых верхнеуровневых видов, изменённых в окне (since, until] (с учётом изменений
     * их происхождений) — для индикатора VTTG.
     */
    @Query("""
            select count(s) from Species s
            where (:srdOnly = false or s.srdVersion is not null)
              and (:srdVersion is null or s.srdVersion = :srdVersion)
              and s.isHiddenEntity = false
              and s.parent is null
              and (
                    (coalesce(s.updatedAt, s.createdAt) > :since and coalesce(s.updatedAt, s.createdAt) <= :until)
                 or exists (
                        select 1 from Species l
                        where l.parent = s
                          and l.isHiddenEntity = false
                          and coalesce(l.updatedAt, l.createdAt) > :since
                          and coalesce(l.updatedAt, l.createdAt) <= :until
                    )
              )
            """)
    long countChangedForVttgExport(@Param("srdVersion") String srdVersion,
                                   @Param("srdOnly") boolean srdOnly,
                                   @Param("since") Instant since,
                                   @Param("until") Instant until);
}
