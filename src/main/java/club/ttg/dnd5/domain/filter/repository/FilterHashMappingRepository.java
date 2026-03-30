package club.ttg.dnd5.domain.filter.repository;

import club.ttg.dnd5.domain.filter.model.FilterHashCategory;
import club.ttg.dnd5.domain.filter.model.FilterHashMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FilterHashMappingRepository extends JpaRepository<FilterHashMapping, String>
{
    List<FilterHashMapping> findAllByHashIn(Collection<String> hashes);

    List<FilterHashMapping> findAllByCategory(FilterHashCategory category);
}
