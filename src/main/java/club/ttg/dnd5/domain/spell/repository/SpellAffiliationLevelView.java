package club.ttg.dnd5.domain.spell.repository;

/**
 * Строка связи заклинания с видом или происхождением: url сущности и уровень, с
 * которого заклинание доступно. Уровень принадлежит стороне вида
 * ({@code species.innateSpells}), но живёт в той же таблице, поэтому сохранение
 * заклинания обязано его знать, чтобы не затереть.
 */
public interface SpellAffiliationLevelView
{
    String getUrl();

    Integer getLevel();
}
