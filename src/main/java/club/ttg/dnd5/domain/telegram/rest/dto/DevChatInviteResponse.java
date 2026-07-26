package club.ttg.dnd5.domain.telegram.rest.dto;

/**
 * Выпущенная одноразовая ссылка-приглашение в секретный чат разработки.
 *
 * @param inviteLink ссылка вида {@code https://t.me/+...}; гаснет после первого входа
 *                   ({@code member_limit=1} на стороне Telegram)
 */
public record DevChatInviteResponse(
        String inviteLink
) {
}
