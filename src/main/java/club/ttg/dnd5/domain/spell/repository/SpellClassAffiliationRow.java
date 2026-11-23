package club.ttg.dnd5.domain.spell.repository;

/**
 * Принадлежность заклинания классу — одной строкой, без загрузки самого класса.
 *
 * <p>Краткому ответу о заклинании от класса нужны только слаг, название и акроним источника.
 * Ленивая связь {@code Spell.classAffiliation} поднимала классы целиком (умения, таблицы,
 * механика) отдельным запросом на каждое заклинание списка.</p>
 *
 * @param spellUrl      слаг заклинания.
 * @param classUrl      слаг класса.
 * @param className     название класса.
 * @param sourceAcronym акроним источника класса; {@code null}, если источника нет.
 */
public record SpellClassAffiliationRow(String spellUrl, String classUrl, String className, String sourceAcronym)
{
}
