package club.ttg.dnd5.domain.item.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Предмет-хранилище: рюкзак, мешок, сундук, Сумка хранения. В VTTG такой предмет на листе
 * персонажа открывается окном, в которое кладут вещи ({@code DnDGameItem.container}).
 *
 * <p>Сам факт наличия объекта делает предмет контейнером; пустой объект — контейнер без
 * ограничения, содержимое которого весит.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemContainer {
    /** Сколько фунтов помещается. Пусто — без ограничения. */
    private Double capacityWeight;
    /** Содержимое не добавляет веса носителю (Сумка хранения). Пусто — содержимое весит. */
    private Boolean weightless;
}
