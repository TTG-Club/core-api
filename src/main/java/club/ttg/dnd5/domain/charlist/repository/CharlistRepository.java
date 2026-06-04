package club.ttg.dnd5.domain.charlist.repository;

import club.ttg.dnd5.domain.charlist.model.Charlist;
import club.ttg.dnd5.domain.charlist.model.CharlistVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CharlistRepository extends JpaRepository<Charlist, UUID> {

    List<Charlist> findAllByOwnerId(UUID ownerId);

    Optional<Charlist> findByShareToken(String shareToken);

    List<Charlist> findAllByVisibility(CharlistVisibility visibility);
}
