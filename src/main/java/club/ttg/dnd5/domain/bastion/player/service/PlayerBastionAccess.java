package club.ttg.dnd5.domain.bastion.player.service;

import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembership;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionEditor;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionEditorRepository;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionRepository;
import club.ttg.dnd5.domain.user.service.UserService;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Общие проверки прав на бастионы игроков. Права в игре берутся из актуального состава игры в
 * find-game-api, а не из записи бастиона; вне игры — из владельца ({@link BastionViewer}).
 */
@Component
@RequiredArgsConstructor
public class PlayerBastionAccess {
    private final PlayerBastionRepository bastionRepository;
    private final PlayerBastionEditorRepository editorRepository;
    private final GameMembershipClient membershipClient;
    private final UserService userService;

    public UUID currentUserId() {
        return userService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован"));
    }

    /** Участник игры — мастер или игрок с одобренной заявкой; иначе 403. */
    public GameMembership requireParticipant(UUID gameId, UUID userId) {
        GameMembership membership = membershipClient.membership(gameId, userId);
        if (!membership.isParticipant()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Бастионы игры видят только её участники");
        }
        return membership;
    }

    /** Кем пользователь приходится бастиону; не может смотреть — 403. */
    public BastionViewer requireViewer(PlayerBastion bastion, UUID userId) {
        boolean editor = !userId.equals(bastion.getOwnerId()) && editorRepository.existsByBastionIdAndUserIdAndStatus(
                bastion.getId(), userId, PlayerBastionEditor.Status.APPROVED);
        BastionViewer viewer = bastion.getGameId() == null
                ? BastionViewer.outsideGame(bastion, userId, editor)
                : BastionViewer.inGame(bastion, membershipClient.membership(bastion.getGameId(), userId), userId,
                        editor);
        if (!viewer.visible()) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Бастион видят его владелец, игроки с доступом и участники его игры");
        }
        return viewer;
    }

    /** Тот, кто ведёт бастион: мастер его игры, а вне игры — владелец; иначе 403. */
    public BastionViewer requireManager(PlayerBastion bastion, UUID userId) {
        BastionViewer viewer = requireViewer(bastion, userId);
        if (!viewer.manager()) {
            throw new ApiException(HttpStatus.FORBIDDEN, bastion.getGameId() == null
                    ? "Это может сделать только владелец бастиона"
                    : "Это может сделать только мастер игры");
        }
        return viewer;
    }

    /** Владелец бастиона; иначе 403. */
    public BastionViewer requireOwner(PlayerBastion bastion, UUID userId) {
        BastionViewer viewer = requireViewer(bastion, userId);
        if (!viewer.owner()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Это может сделать только владелец бастиона");
        }
        return viewer;
    }

    public PlayerBastion findBastion(UUID id) {
        return bastionRepository.findWithMembersById(id)
                .orElseThrow(() -> new EntityNotFoundException("Бастион %s не найден".formatted(id)));
    }

    public static void requireNotArchived(PlayerBastion bastion) {
        if (bastion.getStatus() == PlayerBastionStatus.ARCHIVED) {
            throw new ApiException(HttpStatus.CONFLICT, "Бастион в архиве, изменить его нельзя");
        }
    }

    public static void requireVersion(PlayerBastion bastion, long version) {
        if (bastion.getVersion() != version) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Бастион уже изменили, обновите страницу и повторите правку");
        }
    }
}
