package club.ttg.dnd5.domain.bastion.player.repository;

import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerBastionRepository extends JpaRepository<PlayerBastion, UUID> {

    /** Бастионы игры вместе с участниками, старые первыми. */
    @EntityGraph(attributePaths = "members")
    List<PlayerBastion> findAllByGameIdOrderByCreatedAtAsc(UUID gameId);

    @EntityGraph(attributePaths = "members")
    Optional<PlayerBastion> findWithMembersById(UUID id);

    /** Бастионы владельца вместе с участниками, старые первыми. */
    @EntityGraph(attributePaths = "members")
    List<PlayerBastion> findAllByOwnerIdOrderByCreatedAtAsc(UUID ownerId);

    long countByOwnerId(UUID ownerId);

    /** Бастион по ссылке «поделиться» вместе с участниками. */
    @EntityGraph(attributePaths = "members")
    Optional<PlayerBastion> findWithMembersByShareToken(UUID shareToken);

    /** Бастионы по списку вместе с участниками, старые первыми. */
    @EntityGraph(attributePaths = "members")
    List<PlayerBastion> findAllByIdInOrderByCreatedAtAsc(Collection<UUID> ids);

    /**
     * Удаляет бастион одним запросом: участников, сооружения, план, приказы, ходы и журнал
     * казны убирают каскады базы.
     */
    @Modifying
    @Query("delete from PlayerBastion bastion where bastion.id = :id")
    void deleteBastion(@Param("id") UUID id);
}
