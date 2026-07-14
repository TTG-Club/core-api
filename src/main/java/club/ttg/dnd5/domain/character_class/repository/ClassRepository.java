package club.ttg.dnd5.domain.character_class.repository;

import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.vttg.repository.VttgEntityRef;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClassRepository extends JpaRepository<CharacterClass, String> {

    Collection<CharacterClass> findAllByParentIsNull(final Sort by);

    @Query("""
    SELECT c FROM CharacterClass c
    WHERE c.parent IS NOT NULL
    ORDER BY
      CASE c.source.type
        WHEN 'OFFICIAL' then 1
        WHEN 'SETTING' then 2
        WHEN 'MODULE' then 3
        WHEN 'TEST' then 4
        WHEN 'THIRD_PARTY' then 5
        WHEN 'CUSTOM' then 6
      end,
      c.parent.name,
      c.name
    """)
    Collection<CharacterClass> findAllByParentIsNotNull();

    Optional<CharacterClass> findByUrl(String url);

    List<CharacterClass> findAllByParentIsNullAndCasterTypeNot(CasterType casterType);

    @Query("""
    SELECT distinct cc
    FROM CharacterClass cc
    WHERE cc.parent is not null
      AND cc.casterType <> :casterType
      AND EXISTS (
          SELECT 1
          FROM Spell s
          JOIN s.subclassAffiliation sub
          WHERE sub = cc
      )
    """)
    List<CharacterClass> findAllSubclassesWithSpellAffiliationAndCasterTypeNot(@Param("casterType") CasterType casterType);

    @Query(value = """
        select distinct c.source
        from class c
        where c.source is not null
        order by c.source
        """, nativeQuery = true)
    List<String> findAllUsedSourceCodes();

    @Query(value = """
        select distinct c.srd_version
        from class c
        where c.srd_version is not null
        order by c.srd_version
        """, nativeQuery = true)
    List<String> findDistinctSrdVersions();

    /**
     * Лёгкие ссылки (url + время изменения) классов окна — без гидрации jsonb. Возвращает видимые
     * верхнеуровневые классы (без родителя), изменённые сами либо через свой подкласс (подзапрос
     * {@code exists}), иначе правка подкласса не доехала бы до клиента. Подклассы отдельными
     * записями не выгружаются — они сворачиваются внутрь записи родителя. Время — собственное
     * время класса (см. аналогичный {@code SpeciesRepository#findChangedRefsForVttgExport}).
     */
    @Query("""
            select c.url as url, coalesce(c.updatedAt, c.createdAt) as changedAt from CharacterClass c
            where (:srdOnly = false or c.srdVersion is not null)
              and (:srdVersion is null or c.srdVersion = :srdVersion)
              and c.isHiddenEntity = false
              and c.parent is null
              and (
                    (coalesce(c.updatedAt, c.createdAt) > :since and coalesce(c.updatedAt, c.createdAt) <= :until)
                 or exists (
                        select 1 from CharacterClass sub
                        where sub.parent = c
                          and sub.isHiddenEntity = false
                          and coalesce(sub.updatedAt, sub.createdAt) > :since
                          and coalesce(sub.updatedAt, sub.createdAt) <= :until
                    )
              )
            """)
    List<VttgEntityRef> findChangedRefsForVttgExport(@Param("srdVersion") String srdVersion,
                                                     @Param("srdOnly") boolean srdOnly,
                                                     @Param("since") Instant since,
                                                     @Param("until") Instant until);

    /** Полные классы по набору url — для пересчёта недостающих payload (fallback). */
    @EntityGraph(attributePaths = {"source", "subclasses", "subclasses.source"})
    @Query("select distinct c from CharacterClass c where c.url in :urls")
    List<CharacterClass> findAllForVttgExportByUrls(@Param("urls") Collection<String> urls);

    /**
     * Максимум времени изменения видимых классов (включая подклассы) — «отметка зависимостей»:
     * payload родителя сворачивает данные подклассов, правка подкласса должна его инвалидировать.
     */
    @Query("select max(coalesce(c.updatedAt, c.createdAt)) from CharacterClass c where c.isHiddenEntity = false")
    Instant maxChangedAtForVttgExport();

    /**
     * Число видимых верхнеуровневых классов, изменённых в окне (since, until] (с учётом изменений
     * их подклассов) — для индикатора VTTG.
     */
    @Query("""
            select count(c) from CharacterClass c
            where (:srdOnly = false or c.srdVersion is not null)
              and (:srdVersion is null or c.srdVersion = :srdVersion)
              and c.isHiddenEntity = false
              and c.parent is null
              and (
                    (coalesce(c.updatedAt, c.createdAt) > :since and coalesce(c.updatedAt, c.createdAt) <= :until)
                 or exists (
                        select 1 from CharacterClass sub
                        where sub.parent = c
                          and sub.isHiddenEntity = false
                          and coalesce(sub.updatedAt, sub.createdAt) > :since
                          and coalesce(sub.updatedAt, sub.createdAt) <= :until
                    )
              )
            """)
    long countChangedForVttgExport(@Param("srdVersion") String srdVersion,
                                   @Param("srdOnly") boolean srdOnly,
                                   @Param("since") Instant since,
                                   @Param("until") Instant until);
}
