package club.ttg.dnd5.domain.item.repository;

import club.ttg.dnd5.domain.item.model.Item;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, String>,
        JpaSpecificationExecutor<Item> {
    @Query(value = """
            select i from Item i
            where i.name ilike concat('%', :searchLine, '%')
               or i.english ilike concat('%', :searchLine, '%')
               or i.alternative ilike concat('%', :searchLine, '%')
               or i.name ilike concat('%', :invertedSearchLine, '%')
               or i.english ilike concat('%', :invertedSearchLine, '%')
               or i.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    Collection<Item> findBySearchLine(String searchLine, String invertedSearchLine, Sort sort);

    @Query(value = """
        select distinct i.source
        from item i
        where i.source is not null
        order by i.source
        """, nativeQuery = true)
    List<String> findAllUsedSourceCodes();

    @Query(value = """
        select distinct i.srd_version
        from item i
        where i.srd_version is not null
        order by i.srd_version
        """, nativeQuery = true)
    List<String> findDistinctSrdVersions();

    /**
     * Видимые предметы, изменённые в окне (since, until] — для upserts дельты VTTG.
     * Сортировка по времени изменения выполняется на стороне приложения.
     */
    @EntityGraph(attributePaths = {"source"})
    @Query("""
            select i from Item i
            where (:srdVersion is null or i.srdVersion = :srdVersion)
              and i.isHiddenEntity = false
              and coalesce(i.updatedAt, i.createdAt) > :since
              and coalesce(i.updatedAt, i.createdAt) <= :until
            """)
    List<Item> findChangedForVttgExport(@Param("srdVersion") String srdVersion,
                                        @Param("since") Instant since,
                                        @Param("until") Instant until);

    /**
     * Число видимых предметов, изменённых в окне (since, until] — для индикатора VTTG.
     */
    @Query("""
            select count(i) from Item i
            where (:srdVersion is null or i.srdVersion = :srdVersion)
              and i.isHiddenEntity = false
              and coalesce(i.updatedAt, i.createdAt) > :since
              and coalesce(i.updatedAt, i.createdAt) <= :until
            """)
    long countChangedForVttgExport(@Param("srdVersion") String srdVersion,
                                   @Param("since") Instant since,
                                   @Param("until") Instant until);
}
