package club.ttg.dnd5.domain.tool.tracker.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Лимит трекеров текущего пользователя.
 *
 * @param limit           сколько неудалённых трекеров можно держать сейчас
 * @param subscriberLimit сколько даёт подписка: подсказка «с подпиской доступно больше»
 */
@Schema(description = "Лимит трекеров инициативы")
public record TrackerLimitsResponse(int limit, int subscriberLimit) {
}
