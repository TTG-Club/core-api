package club.ttg.dnd5.domain.tool.sheet.service;

/**
 * Лимиты листов персонажей одного пользователя, посчитанные разом: подписка спрашивается
 * у subscriber-service один раз на операцию, а не по разу на каждый лимит.
 *
 * @param activeSheets         максимум активных (неудалённых) листов
 * @param savedSheets          максимум чужих листов, сохранённых по ссылке
 * @param deletedHistory       глубина истории удалённых листов — её видит клиент
 * @param deletedHistoryToTrim глубина, по которую история подрезается при удалении листа.
 *                             Отличается от {@code deletedHistory} только когда статус подписки
 *                             неизвестен: вытеснение стирает листы безвозвратно, поэтому при
 *                             недоступном subscriber-service подрезаем по лимиту подписчика —
 *                             чужой сбой не должен стоить пользователю истории. Показываем при
 *                             этом базовую глубину: выдавать платный лимит наугад нельзя.
 */
public record SheetLimits(int activeSheets, int savedSheets, int deletedHistory, int deletedHistoryToTrim) {
}
