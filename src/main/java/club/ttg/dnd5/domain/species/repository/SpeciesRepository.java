package club.ttg.dnd5.domain.species.repository;

import club.ttg.dnd5.domain.species.model.Species;
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
     * Видимые верхнеуровневые виды (без родителя), изменённые в окне (since, until] — для upserts
     * дельты VTTG. Происхождения (дочерние виды) отдельными записями не выгружаются: они
     * сворачиваются в {@code choices} умения родителя. Родитель попадает в дельту и тогда, когда
     * в окне изменилось только его происхождение (подзапрос {@code exists}), иначе правка дочернего
     * вида не доехала бы до клиента. Сортировка по времени изменения — на стороне приложения.
     */
    @EntityGraph(attributePaths = {"source", "lineages"})
    @Query("""
            select distinct s from Species s
            where (:srdVersion is null or s.srdVersion = :srdVersion)
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
    List<Species> findChangedForVttgExport(@Param("srdVersion") String srdVersion,
                                           @Param("since") Instant since,
                                           @Param("until") Instant until);

    /**
     * Число видимых верхнеуровневых видов, изменённых в окне (since, until] (с учётом изменений
     * их происхождений) — для индикатора VTTG.
     */
    @Query("""
            select count(s) from Species s
            where (:srdVersion is null or s.srdVersion = :srdVersion)
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
                                   @Param("since") Instant since,
                                   @Param("until") Instant until);
}
