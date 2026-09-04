package club.ttg.dnd5.domain.species.model.mechanics;

import club.ttg.dnd5.domain.common.model.mechanics.GrantingMechanics;
import club.ttg.dnd5.domain.common.model.mechanics.MechanicChoice;
import club.ttg.dnd5.domain.common.model.mechanics.ProficiencyGrant;
import club.ttg.dnd5.domain.common.model.mechanics.ResourceCounter;
import club.ttg.dnd5.domain.common.model.mechanics.SheetModifiers;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListExpansion;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Механика влияния вида на лист персонажа — по образцу {@code FeatMechanics}: те же
 * блоки, потому что лист применяет их одинаково, независимо от того, пришли они от
 * вида или от черты.
 *
 * <p>Одна модель на два места. У самой записи ({@code species.mechanics}) — то, что даёт
 * выбор вида или происхождения целиком: сопротивление инфернального тифлинга, скорость
 * лесного эльфа. У умения ({@code species.features[].mechanics}) — то, что даёт
 * конкретное умение: дварфийская стойкость, владение навыком.</p>
 *
 * <p>Две точки, а не одна, потому что происхождения справочника умений не имеют вовсе:
 * их правило целиком лежит в описании, и приписать сопротивление там было бы некуда.
 * Виду с умениями обычно нужна вторая точка — она показывает игроку, какое именно умение
 * дало эффект.</p>
 *
 * <p>Повышения характеристик здесь нет, в отличие от черты: по правилам 2024 года
 * характеристики поднимает предыстория, а вид не поднимает их ни одним умением
 * справочника. Появится такой вид — блок добавится, как он добавлялся у черт.</p>
 *
 * <p>Выдачи заклинаний здесь тоже нет: заклинания вида живут в связующей таблице
 * ({@code innateSpells}) со своими требуемыми уровнями, и второе место для того же
 * самого только расходилось бы с первым. Расширение списка ({@link #spellList}) — другая
 * механика: не знание, а доступность, и в таблице ему места нет.</p>
 *
 * <p>{@code null} — у записей, сохранённых до появления поля, и у тех, чьё действие
 * описано только текстом. Условные и разовые эффекты сюда не идут по тем же причинам,
 * что и у черт: «преимущество на проверки Силы, пока длится превращение» — это не число
 * в шапке листа, и лист показывает такое справкой.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpeciesMechanics implements GrantingMechanics {
    /**
     * Постоянные модификаторы листа: чувства, сопротивления, скорости, хиты, КД,
     * тип существа. Тёмное зрение — тоже чувство здесь ({@code senses}, тип
     * {@code DARKVISION}): его дарит умение, своего поля у записи нет, а деталь
     * отдаёт в {@code properties.darkVision} вычисленный максимум по механике.
     */
    @Schema(description = "Постоянные модификаторы листа персонажа",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private SheetModifiers modifiers;

    /**
     * Владения, которые выдаются без выбора: «Вы получаете владение навыком
     * Внимательность». Выбираемые владения живут в {@link #choices}.
     */
    @Schema(description = "Владения, которые выдаются без выбора",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ProficiencyGrant proficiencies;

    /**
     * Выборы, которые игрок делает при выборе вида или происхождения: навык, тип урона,
     * заклинательная характеристика.
     */
    @Schema(description = "Выборы, которые игрок делает при выборе вида",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<MechanicChoice> choices;

    /**
     * Ресурсы со своим счётчиком на листе: «Дыхание дракона» — бонус мастерства раз до
     * продолжительного отдыха, «Исцеляющие руки» аасимара. Та же модель, что у черты и
     * умения класса: лист и выгрузка VTTG заводят счётчик одинаково, откуда бы он ни пришёл.
     */
    @Schema(description = "Ресурсы со своим счётчиком на листе",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ResourceCounter> counters;

    /**
     * Расширение списка заклинаний класса: заклинания, которые вид или его умение добавляет
     * к списку персонажа, — он может их выучить или подготовить, но готовыми не знает. Той
     * же моделью, что у черты и умения класса: лист и выгрузка VTTG читают её одним кодом.
     */
    @Schema(description = "Расширение списка заклинаний класса",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private SpellListExpansion spellList;
}
