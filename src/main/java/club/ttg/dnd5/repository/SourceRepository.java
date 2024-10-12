package club.ttg.dnd5.repository;

import club.ttg.dnd5.model.book.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceRepository extends JpaRepository<Source, String> {
}
