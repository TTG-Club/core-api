package club.ttg.dnd5.domain.filter.repository;

import club.ttg.dnd5.domain.filter.model.AbstractSavedFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface AbstractSavedFilterRepository<T extends AbstractSavedFilter> extends JpaRepository<T, UUID> {
    @Query("""
            select sf from #{#entityName} sf where sf.userId = :userId and sf.defaultFilter = true
            """)
    Optional<T> findByUserIdAndDefaultFilterTrue(UUID userId);
}
