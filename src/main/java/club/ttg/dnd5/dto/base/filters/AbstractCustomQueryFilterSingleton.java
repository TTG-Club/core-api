package club.ttg.dnd5.dto.base.filters;

/**
 * Абстрактный базовый класс для одиночных фильтров с пользовательскими запросами.
 * Наследует функциональность {@link AbstractCustomQueryFilterItem} и предоставляет
 * специализированную реализацию для одиночных фильтров (например-концентрация в заклинаниях).
 *
 * <p>Используется для фильтров, которые по своей природе могут иметь только одно значение
 * или состояние в рамках группы.</p>
 */
public abstract class AbstractCustomQueryFilterSingleton extends AbstractCustomQueryFilterItem {

    /**
     * Создает новый экземпляр одиночного фильтра.
     *
     * @param name имя фильтра для идентификации
     * @param selected флаг выбора:
     *        null - не выбран (UNCHECKED),
     *        true - выбран (POSITIVE),
     *        false - исключен (NEGATIVE)
     */
    public AbstractCustomQueryFilterSingleton(String name, Boolean selected) {
        super(name, selected);
    }
}
