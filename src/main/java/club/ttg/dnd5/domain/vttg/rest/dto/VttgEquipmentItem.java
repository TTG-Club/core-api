package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Позиция варианта стартового снаряжения в формате компендиума VTTG.
 *
 * <p>Идёт рядом с готовой строкой варианта ({@code description}), а не вместо неё: строка
 * нужна для чтения — в ней живые ссылки на карточки сайта, — а позиции для того, чтобы
 * мастер настройки положил предметы в инвентарь сам. Раньше структурное снаряжение
 * источника схлопывалось в одну строку, и автовыдать по ней было нечего.</p>
 *
 * @param url         слаг страницы предмета на сайте ({@code dagger-phb}) — по нему позиция
 *                    ищется в компендиуме. Пусто — свободное уточнение вроде «древние карты»:
 *                    карточки за ним нет, и потребитель заводит предмет просто по названию
 * @param name        название предмета
 * @param quantity    количество; опускается, когда предмет один
 * @param description уточнение к позиции («по вашему выбору»)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VttgEquipmentItem(String url, String name, Integer quantity, String description) {
}
