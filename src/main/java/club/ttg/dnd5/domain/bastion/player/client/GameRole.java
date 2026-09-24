package club.ttg.dnd5.domain.bastion.player.client;

/** Кем пользователь приходится игре каталога (ответ find-game-api). */
public enum GameRole {
    /** Мастер игры. */
    MASTER,
    /** Игрок с одобренной заявкой. */
    PLAYER,
    /** Не участник. */
    NONE
}
