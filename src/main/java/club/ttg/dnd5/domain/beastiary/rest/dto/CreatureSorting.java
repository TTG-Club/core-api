package club.ttg.dnd5.domain.beastiary.rest.dto;

public enum CreatureSorting
{
    CHALLENGE_RATING,
    NAME,
    ENGLISH,
    /**
     * По пользовательскому рейтингу: средняя оценка, затем число оценок, обе по убыванию.
     */
    RATING
}
