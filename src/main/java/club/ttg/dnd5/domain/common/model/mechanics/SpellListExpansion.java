package club.ttg.dnd5.domain.common.model.mechanics;

import club.ttg.dnd5.domain.common.model.EntityRef;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Заклинания, которые черта добавляет в список заклинаний класса — таблица «Заклинания
 * метки» у черт метки дракона.
 *
 * <p>Отдельным блоком от {@link SpellGrant}, потому что это другая механика. Выданное
 * заклинание игрок знает и накладывает; заклинание из этого списка он только МОЖЕТ
 * подготовить наравне с классовыми — потратив на него подготовку и ячейку. Свалить их в
 * одну кучу значило бы выдать «Метке исцеления» девять готовых заклинаний вместо двух.</p>
 *
 * <p>Круг заклинания здесь не хранится, хотя в книге таблица разбита по кругам: круг —
 * свойство самой записи справочника, и снимок разошёлся бы с каталогом при первой же
 * правке заклинания. Потребитель группирует список по кругу сам, взяв его из записи —
 * ровно так же, как деталь черты добирает круг и школу выданным заклинаниям.</p>
 *
 * <p>Заклинания лежат в {@link SpellListGroup списках}: у части черт таблица открывается
 * ступенями, и из каждой ступени берут ограниченное число заклинаний. Плоское поле
 * {@link #getSpells()} — прежняя форма того же блока; читать блок надо через
 * {@link #resolveGroups()}, чтобы обе формы разбирались одинаково.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpellListExpansion {
    /**
     * Списки заклинаний по уровням доступа. Несколько — это НЕ «или»: каждый открывается
     * на своём уровне и складывается с предыдущими.
     */
    @Schema(description = "Списки заклинаний по уровням доступа")
    private List<SpellListGroup> groups;

    /**
     * Все заклинания блока подряд, без разбивки на списки, — прежняя форма.
     *
     * <p>Осталась ради записей, сохранённых до появления уровней доступа: у них весь блок
     * лежит здесь, и выбросить поле значило бы потерять им заклинания. Новые записи
     * пишут {@link #getGroups()}; читать надо через {@link #resolveGroups()}.</p>
     */
    @Schema(description = "Заклинания блока без разбивки на списки (прежняя форма)")
    private List<EntityRef> spells;

    /**
     * Список расширяется, только если у персонажа есть умение «Использование заклинаний»
     * или «Магия договора».
     *
     * <p>Так написано у всех черт метки дракона: без своего заклинательства расширять
     * нечего. {@code null} читается как «расширяет всегда» — на случай черты, которой
     * такое условие не поставили.</p>
     */
    @Schema(description = "Нужно умение «Использование заклинаний» или «Магия договора»")
    private Boolean requiresSpellcasting;

    /**
     * Списки блока с поправкой на прежнюю форму: запись без {@link #getGroups()} читается
     * как один список — доступен сразу, берётся целиком.
     *
     * <p>Приведение здесь, а не у каждого потребителя: две формы одного блока обязаны
     * разбираться одним кодом, иначе деталь черты и выгрузка разойдутся на первой же
     * записи, которую не успели пересохранить.</p>
     *
     * @return списки заклинаний; пусто — черта список не расширяет.
     */
    @JsonIgnore
    public List<SpellListGroup> resolveGroups() {
        if (!CollectionUtils.isEmpty(groups)) {
            return groups;
        }
        if (CollectionUtils.isEmpty(spells)) {
            return List.of();
        }
        SpellListGroup legacy = new SpellListGroup();
        legacy.setSpells(spells);
        return List.of(legacy);
    }
}
