package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Ссылка на другую запись справочника в формате компендиума VTTG.
 *
 * <p>Кроме слага страницы несёт снимок названия: потребитель сверяет требование и с
 * ключом записи на листе, и с её названием — ключ у него голый ({@code wizard}), а слаг
 * сайта тащит суффикс источника ({@code wizard-phb}). Название вдобавок позволяет
 * показать требование, даже если самой записи в паках нет.</p>
 *
 * @param url  слаг страницы записи на сайте
 * @param name название на момент выгрузки
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VttgEntityRef(String url, String name) {
}
