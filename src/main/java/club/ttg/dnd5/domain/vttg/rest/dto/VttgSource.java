package club.ttg.dnd5.domain.vttg.rest.dto;

/**
 * Источник в вокабуляре VTTG ({@code SourceDefinition}).
 *
 * <p>Записи компендиума везут только ключ источника ({@code sourceKey}); по нему из этого
 * словаря берётся подпись в карточке и вариант в выпадающем списке при создании своего
 * контента. Без словаря приложение знает лишь встроенные PHB/DMG/MM/HB, и всё остальное
 * ({@code lfl}, {@code efa}, …) остаётся безымянным.</p>
 *
 * @param key          ключ, совпадающий с {@code sourceKey} записей (см. {@code VttgSourceKeys})
 * @param name         русское название
 * @param nameEn       английское название
 * @param abbreviation аббревиатура для компактной подписи
 */
public record VttgSource(String key, String name, String nameEn, String abbreviation) {
}
