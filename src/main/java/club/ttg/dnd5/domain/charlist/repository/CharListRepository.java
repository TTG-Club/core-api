package club.ttg.dnd5.domain.charlist.repository;

import club.ttg.dnd5.domain.charlist.model.CharList;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface CharListRepository extends CrudRepository<CharList, String> {

    long countByCreatedBy(String username);

    Collection<CharList> findByCreatedBy(String username);
}
