package club.ttg.dnd5.domain.bastion.player.model;

/** Жизненный цикл бастиона игрока. */
public enum PlayerBastionStatus {
    /** Закладка: игроки выбирают сооружения и рисуют первый план. */
    SETUP,
    /** Идут ходы бастиона. */
    ACTIVE,
    /** Игра закончилась: бастион только для просмотра. */
    ARCHIVED
}
