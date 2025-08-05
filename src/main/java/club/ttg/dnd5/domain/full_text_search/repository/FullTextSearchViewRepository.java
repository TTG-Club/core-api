package club.ttg.dnd5.domain.full_text_search.repository;

import club.ttg.dnd5.domain.full_text_search.model.FullTextSearchView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;

public interface FullTextSearchViewRepository extends JpaRepository<FullTextSearchView, Long> {

    @Query(value = """
        select ftsv from FullTextSearchView ftsv
        where ftsv.name ilike concat('%', :searchLine, '%')
           or ftsv.english ilike concat('%', :searchLine, '%')
           or ftsv.alternative ilike concat('%', :searchLine, '%')
           or ftsv.name ilike concat('%', :invertedSearchLine, '%')
           or ftsv.english ilike concat('%', :invertedSearchLine, '%')
           or ftsv.alternative ilike concat('%', :invertedSearchLine, '%')
        order by
            case when ftsv.name ilike concat('%', :searchLine, '%') then 1 else 0 end +
            case when ftsv.english ilike concat('%', :searchLine, '%') then 1 else 0 end +
            case when ftsv.alternative ilike concat('%', :searchLine, '%') then 1 else 0 end +
            case when ftsv.name ilike concat('%', :invertedSearchLine, '%') then 1 else 0 end +
            case when ftsv.english ilike concat('%', :invertedSearchLine, '%') then 1 else 0 end +
            case when ftsv.alternative ilike concat('%', :invertedSearchLine, '%') then 1 else 0 end desc
        """)
    Collection<FullTextSearchView> findBySearchLine(String searchLine, String invertedSearchLine);


}
