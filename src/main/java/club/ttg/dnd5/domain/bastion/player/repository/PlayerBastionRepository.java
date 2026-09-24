package club.ttg.dnd5.domain.bastion.player.repository;

import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerBastionRepository extends JpaRepository<PlayerBastion, UUID> {

    /** Бастионы игры вместе с участниками, старые первыми. */
    @EntityGraph(attributePaths = "members")
    List<PlayerBastion> findAllByGameIdOrderByCreatedAtAsc(UUID gameId);

    @EntityGraph(attributePaths = "members")
    Optional<PlayerBastion> findWithMembersById(UUID id);
}
