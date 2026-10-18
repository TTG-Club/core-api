package club.ttg.dnd5.domain.bastion.player.repository;

import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionEditor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerBastionEditorRepository extends JpaRepository<PlayerBastionEditor, UUID> {

    Optional<PlayerBastionEditor> findByBastionIdAndUserId(UUID bastionId, UUID userId);

    boolean existsByBastionIdAndUserIdAndStatus(UUID bastionId, UUID userId, PlayerBastionEditor.Status status);

    /** Запросы и выданные права бастиона, старые первыми. */
    List<PlayerBastionEditor> findAllByBastionIdOrderByCreatedAtAsc(UUID bastionId);

    long countByBastionIdAndStatus(UUID bastionId, PlayerBastionEditor.Status status);

    /** Бастионы, которые пользователю разрешили править. */
    @Query("select editor.bastionId from PlayerBastionEditor editor "
            + "where editor.userId = :userId and editor.status = :status")
    List<UUID> findBastionIdsByUserIdAndStatus(@Param("userId") UUID userId,
                                               @Param("status") PlayerBastionEditor.Status status);

    @Modifying
    @Query("delete from PlayerBastionEditor editor where editor.bastionId = :bastionId")
    void deleteAllByBastionId(@Param("bastionId") UUID bastionId);
}
