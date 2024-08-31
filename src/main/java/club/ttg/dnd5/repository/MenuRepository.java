package club.ttg.dnd5.repository;

import club.ttg.dnd5.model.engine.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, String> {
    Optional<Menu> findByUrl(String url);
    boolean existsByUrl(String url);
    void deleteByUrl(String url);
}

