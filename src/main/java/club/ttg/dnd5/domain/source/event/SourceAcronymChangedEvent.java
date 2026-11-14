package club.ttg.dnd5.domain.source.event;

/**
 * У источника сменился акроним. Публикуется внутри транзакции переименования;
 * слушатели, которым нужен уже записанный результат, ждут её фиксации.
 *
 * @param oldAcronym прежний акроним
 * @param newAcronym новый акроним
 * @param username   администратор, сменивший акроним; {@code null}, если неизвестен
 */
public record SourceAcronymChangedEvent(String oldAcronym, String newAcronym, String username) {
}
