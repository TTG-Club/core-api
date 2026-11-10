package club.ttg.dnd5.domain.bastion.player.service;

import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembership;
import club.ttg.dnd5.domain.bastion.player.client.GameRole;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;

import java.util.UUID;

/**
 * Кем открывший приходится бастиону.
 *
 * <p>Бастион в игре ведёт мастер игры — по актуальному составу игры в find-game-api. Бастион
 * вне игры ведёт его владелец и те, кому он разрешил править бастион по ссылке.</p>
 *
 * @param userId  открывший; {@code null} — аноним, открывший бастион по ссылке
 * @param owner   владелец бастиона — тот, кто его создал
 * @param manager ведёт бастион: меняет состав, казну, подтверждает требования, делает ходы
 * @param member  игрок с доступом к бастиону, у которого здесь персонаж
 * @param editor  владелец разрешил ему править бастион по ссылке
 * @param visible может смотреть бастион
 */
public record BastionViewer(UUID userId, boolean owner, boolean manager, boolean member, boolean editor,
                            boolean visible) {

    /**
     * Права на бастион в игре по роли открывшего в этой игре. Игрок, чью заявку отозвали,
     * теряет доступ, даже если ещё числится в составе бастиона; владелец видит свой бастион
     * всегда.
     */
    public static BastionViewer inGame(PlayerBastion bastion, GameMembership membership, UUID userId) {
        return inGame(bastion, membership, userId, false);
    }

    /** Права на бастион в игре; редактор по ссылке рисует план и отдаёт приказы, как игрок. */
    public static BastionViewer inGame(PlayerBastion bastion, GameMembership membership, UUID userId,
                                       boolean editor) {
        boolean owner = userId.equals(bastion.getOwnerId());
        boolean member = isMember(bastion, userId);
        return new BastionViewer(userId, owner, membership.role() == GameRole.MASTER, member, editor,
                owner || editor || membership.isParticipant());
    }

    /** Права на бастион вне игры: ведёт владелец, смотрят владелец и игроки с доступом. */
    public static BastionViewer outsideGame(PlayerBastion bastion, UUID userId) {
        return outsideGame(bastion, userId, false);
    }

    /** Права на бастион вне игры; редактор по ссылке ведёт его наравне с владельцем. */
    public static BastionViewer outsideGame(PlayerBastion bastion, UUID userId, boolean editor) {
        boolean owner = userId.equals(bastion.getOwnerId());
        boolean member = isMember(bastion, userId);
        return new BastionViewer(userId, owner, owner || editor, member, editor, owner || member || editor);
    }

    /**
     * Смотрит бастион по ссылке «поделиться»: только чтение, никаких прав, кроме владельца,
     * который узнаёт свой бастион.
     *
     * @param userId открывший; {@code null} — аноним
     */
    public static BastionViewer byLink(PlayerBastion bastion, UUID userId) {
        return new BastionViewer(userId, bastion.getOwnerId().equals(userId), false, false, false, true);
    }

    /** Может рисовать план и отдавать приказы: тот, кто ведёт бастион, игрок с доступом или редактор. */
    public boolean canEdit() {
        return manager || member || editor;
    }

    /** Сам ли открывший — этот игрок бастиона. */
    public boolean is(UUID memberUserId) {
        return userId != null && userId.equals(memberUserId);
    }

    private static boolean isMember(PlayerBastion bastion, UUID userId) {
        return bastion.getMembers().stream().anyMatch(item -> item.getUserId().equals(userId));
    }
}
