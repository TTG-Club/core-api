package club.ttg.dnd5.domain.source.repository;

import club.ttg.dnd5.domain.source.model.Source;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SourceRepository extends JpaRepository<Source, String> {
    Optional<Source> findByUrl(String url);

}