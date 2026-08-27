package club.ttg.dnd5.domain.species.repository;

import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.vttg.repository.VttgEntityRef;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface SpeciesRepository extends JpaRepository<Species, String> {
    Collection<Species> findByParent(Species parent);

    @Query(value = """
            select spell_url as "spellUrl", required_level as "requiredLevel"
            from spell_species_affiliation
            where species_affiliation_url = :speciesUrl
            union all
            select spell_url as "spellUrl", required_level as "requiredLevel"
            from spell_lineages_affiliation
            where lineages_affiliation_url = :speciesUrl
            order by "requiredLevel", "spellUrl"
            """, nativeQuery = true)
    List<SpeciesInnateSpellView> findInnateSpells(@Param("speciesUrl") String speciesUrl);

    @Modifying
    @Query(value = "delete from spell_species_affiliation where species_affiliation_url = :speciesUrl", nativeQuery = true)
    void deleteSpeciesInnateSpells(@Param("speciesUrl") String speciesUrl);

    @Modifying
    @Query(value = "delete from spell_lineages_affiliation where lineages_affiliation_url = :speciesUrl", nativeQuery = true)
    void deleteLineageInnateSpells(@Param("speciesUrl") String speciesUrl);

    @Modifying
    @Query(value = """
            insert into spell_species_affiliation (spell_url, species_affiliation_url, required_level)
            values (:spellUrl, :speciesUrl, :requiredLevel)
            """, nativeQuery = true)
    void addSpeciesInnateSpell(@Param("speciesUrl") String speciesUrl,
                               @Param("spellUrl") String spellUrl,
                               @Param("requiredLevel") Integer requiredLevel);

    @Modifying
    @Query(value = """
            insert into spell_lineages_affiliation (spell_url, lineages_affiliation_url, required_level)
            values (:spellUrl, :speciesUrl, :requiredLevel)
            """, nativeQuery = true)
    void addLineageInnateSpell(@Param("speciesUrl") String speciesUrl,
                               @Param("spellUrl") String spellUrl,
                               @Param("requiredLevel") Integer requiredLevel);

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
     * виды, изменённые в окне, включая происхождения: каждое происхождение — самостоятельная
     * запись выгрузки со ссылкой {@code parentKey} на родителя, и правка происхождения меняет
     * только его собственный payload. Время — собственное время вида.
     */
    @Query("""
            select s.url as url, coalesce(s.updatedAt, s.createdAt) as changedAt from Species s
            where (:srdOnly = false or s.srdVersion is not null)
              and (:srdVersion is null or s.srdVersion = :srdVersion)
              and s.isHiddenEntity = false
              and coalesce(s.updatedAt, s.createdAt) > :since
              and coalesce(s.updatedAt, s.createdAt) <= :until
            """)
    List<VttgEntityRef> findChangedRefsForVttgExport(@Param("srdVersion") String srdVersion,
                                                     @Param("srdOnly") boolean srdOnly,
                                                     @Param("since") Instant since,
                                                     @Param("until") Instant until);

    /** Полные виды по набору url — для пересчёта недостающих payload (fallback). */
    @EntityGraph(attributePaths = {"source", "parent"})
    @Query("select distinct s from Species s where s.url in :urls")
    List<Species> findAllForVttgExportByUrls(@Param("urls") Collection<String> urls);

    /** Максимум времени изменения видимых видов — «отметка зависимостей» кэша выгрузки. */
    @Query("select max(coalesce(s.updatedAt, s.createdAt)) from Species s where s.isHiddenEntity = false")
    Instant maxChangedAtForVttgExport();

    /** Число видимых видов (включая происхождения), изменённых в окне (since, until] — для индикатора VTTG. */
    @Query("""
            select count(s) from Species s
            where (:srdOnly = false or s.srdVersion is not null)
              and (:srdVersion is null or s.srdVersion = :srdVersion)
              and s.isHiddenEntity = false
              and coalesce(s.updatedAt, s.createdAt) > :since
              and coalesce(s.updatedAt, s.createdAt) <= :until
            """)
    long countChangedForVttgExport(@Param("srdVersion") String srdVersion,
                                   @Param("srdOnly") boolean srdOnly,
                                   @Param("since") Instant since,
                                   @Param("until") Instant until);
}
