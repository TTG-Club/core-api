package club.ttg.dnd5.domain.filter.repository;

import club.ttg.dnd5.domain.filter.model.AbstractSavedFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface SavedFilterRepository<T extends AbstractSavedFilter> extends JpaRepository<T, UUID> {
    @Query("""
            SELECT sf FROM #{#entityName} sf WHERE sf.userId = :userId AND sf.defaultFilter = TRUE
            """)
    Optional<T> findByUserIdAndDefaultFilterTrue(UUID userId);
}
