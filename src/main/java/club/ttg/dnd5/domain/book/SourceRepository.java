package club.ttg.dnd5.domain.book;

import club.ttg.dnd5.domain.book.model.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {

}
