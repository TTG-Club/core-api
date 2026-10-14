package club.ttg.dnd5.domain.bastion.player.service;

import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembership;
import club.ttg.dnd5.domain.bastion.player.client.GameRole;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;

import java.util.UUID;

/**
 * Кем открывший приходится бастиону.
 *
 * <p>Бастион в игре ведёт мастер игры — по актуальному составу игры в find-game-api. Бастион
 * вне игры ведёт его владелец.</p>
 *
 * @param userId  открывший
 * @param owner   владелец бастиона — тот, кто его создал
 * @param manager ведёт бастион: меняет состав, казну, подтверждает требования, делает ходы
 * @param member  игрок с доступом к бастиону, у которого здесь персонаж
 * @param visible может смотреть бастион
 */
public record BastionViewer(UUID userId, boolean owner, boolean manager, boolean member, boolean visible) {

    /**
     * Права на бастион в игре по роли открывшего в этой игре. Игрок, чью заявку отозвали,
     * теряет доступ, даже если ещё числится в составе бастиона; владелец видит свой бастион
     * всегда.
     */
    public static BastionViewer inGame(PlayerBastion bastion, GameMembership membership, UUID userId) {
        boolean owner = userId.equals(bastion.getOwnerId());
        boolean member = isMember(bastion, userId);
        return new BastionViewer(userId, owner, membership.role() == GameRole.MASTER, member,
                owner || membership.isParticipant());
    }

    /** Права на бастион вне игры: ведёт владелец, смотрят владелец и игроки с доступом. */
    public static BastionViewer outsideGame(PlayerBastion bastion, UUID userId) {
        boolean owner = userId.equals(bastion.getOwnerId());
        boolean member = isMember(bastion, userId);
        return new BastionViewer(userId, owner, owner, member, owner || member);
    }

    /** Может рисовать план и отдавать приказы: тот, кто ведёт бастион, или игрок с доступом. */
    public boolean canEdit() {
        return manager || member;
    }

    /** Сам ли открывший — этот игрок бастиона. */
    public boolean is(UUID memberUserId) {
        return userId.equals(memberUserId);
    }

    private static boolean isMember(PlayerBastion bastion, UUID userId) {
        return bastion.getMembers().stream().anyMatch(item -> item.getUserId().equals(userId));
    }
}
