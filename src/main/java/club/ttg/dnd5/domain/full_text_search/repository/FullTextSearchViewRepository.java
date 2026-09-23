package club.ttg.dnd5.domain.full_text_search.repository;

import club.ttg.dnd5.domain.full_text_search.model.FullTextSearchView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface FullTextSearchViewRepository extends JpaRepository<FullTextSearchView, Long> {

    @Query(value = """
        select ftsv from FullTextSearchView ftsv
        where replace(replace(ftsv.name, 'Ё', 'Е'), 'ё', 'е') ilike concat('%', :searchLine, '%')
           or ftsv.english ilike concat('%', :searchLine, '%')
           or replace(replace(ftsv.alternative, 'Ё', 'Е'), 'ё', 'е') ilike concat('%', :searchLine, '%')
           or replace(replace(ftsv.name, 'Ё', 'Е'), 'ё', 'е') ilike concat('%', :invertedSearchLine, '%')
           or ftsv.english ilike concat('%', :invertedSearchLine, '%')
           or replace(replace(ftsv.alternative, 'Ё', 'Е'), 'ё', 'е') ilike concat('%', :invertedSearchLine, '%')
        order by
            case when replace(replace(ftsv.name, 'Ё', 'Е'), 'ё', 'е') ilike concat('%', :searchLine, '%') then 1 else 0 end +
            case when ftsv.english ilike concat('%', :searchLine, '%') then 1 else 0 end +
            case when replace(replace(ftsv.alternative, 'Ё', 'Е'), 'ё', 'е') ilike concat('%', :searchLine, '%') then 1 else 0 end +
            case when replace(replace(ftsv.name, 'Ё', 'Е'), 'ё', 'е') ilike concat('%', :invertedSearchLine, '%') then 1 else 0 end +
            case when ftsv.english ilike concat('%', :invertedSearchLine, '%') then 1 else 0 end +
            case when replace(replace(ftsv.alternative, 'Ё', 'Е'), 'ё', 'е') ilike concat('%', :invertedSearchLine, '%') then 1 else 0 end desc
        """)
    Collection<FullTextSearchView> findBySearchLine(String searchLine, String invertedSearchLine);

    @Query("""
        select ftsv from FullTextSearchView ftsv
        order by coalesce(ftsv.updatedAt, ftsv.createdAt) desc
        """)
    List<FullTextSearchView> findTop10LatestUpdatedOrCreated(org.springframework.data.domain.Pageable pageable);

}
