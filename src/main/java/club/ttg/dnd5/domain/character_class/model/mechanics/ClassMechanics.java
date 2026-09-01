package club.ttg.dnd5.domain.character_class.model.mechanics;

import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.common.model.mechanics.GrantingMechanics;
import club.ttg.dnd5.domain.common.model.mechanics.MechanicChoice;
import club.ttg.dnd5.domain.common.model.mechanics.ProficiencyGrant;
import club.ttg.dnd5.domain.common.model.mechanics.ResourceCounter;
import club.ttg.dnd5.domain.common.model.mechanics.SheetModifiers;
import club.ttg.dnd5.domain.common.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListExpansion;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Механика влияния класса на лист персонажа — по образцу {@code SpeciesMechanics} и
 * {@code FeatMechanics}: те же блоки, потому что лист применяет их одинаково, откуда бы
 * они ни пришли.
 *
 * <p>Одна модель на две точки. У самой записи ({@code class.mechanics}) — то, что даёт
 * взятие класса или подкласса целиком: владения первого уровня уже описаны отдельными
 * полями записи, поэтому здесь остаётся то, чему в них места нет, — ресурсы, расширение
 * списка заклинаний, выборы при взятии. У умения ({@code class.features[].mechanics}) —
 * то, что даёт конкретное умение на своём уровне: «Экспертиза» плута, «Метка охотника»
 * следопыта, очки чародейства.</p>
 *
 * <p>Повышений характеристик здесь нет, в отличие от черты: по правилам 2024 года это
 * черта «Улучшение характеристик», и умение выдаёт её выбором черты
 * ({@code ChoiceType.FEAT}). Флаг {@code ClassFeature.abilityImprovement} остаётся
 * запасным признаком для записей и потребителей, которые про выбор черты не знают.</p>
 *
 * <p><b>Формат — контракт с сайтом.</b> Ответ детальника и тело запроса мастерской отдают
 * эту модель как есть ({@code ClassDetailedResponse.mechanics}, {@code ClassRequest.mechanics}),
 * поэтому имена полей и значения enum'ов менять нельзя молча: это ломает и редактор, и уже
 * сохранённый JSONB.</p>
 *
 * <p>{@code null} — у классов, сохранённых до появления поля, и у тех, чьё действие
 * описано только текстом.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassMechanics implements GrantingMechanics {
    /**
     * Постоянные модификаторы листа: хиты, скорости, КД, чувства, защиты. Условные
     * («Защита без доспехов» варвара считает КД только без доспеха) сюда не идут —
     * их выражает активный эффект со своим условием.
     */
    @Schema(description = "Постоянные модификаторы листа персонажа",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private SheetModifiers modifiers;

    /**
     * Владения, которые умение выдаёт без выбора. Владения первого уровня самого класса
     * сюда не дублируются — они живут отдельными полями записи ({@code armorProficiency},
     * {@code weaponProficiency}, {@code skillProficiency}, {@code savingThrows}).
     */
    @Schema(description = "Владения, которые выдаются без выбора",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ProficiencyGrant proficiencies;

    /**
     * Выборы, которые игрок делает при получении: навык для «Экспертизы», тип урона,
     * заклинательная характеристика, черта — боевой стиль или черта за повышение
     * характеристик ({@code ChoiceType.FEAT}). Флаг умения {@code fightingStyleChoice}
     * остаётся запасным признаком для записей, где выбор черты ещё не описан.
     */
    @Schema(description = "Выборы, которые игрок делает при получении",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<MechanicChoice> choices;

    /**
     * Заклинания, которые класс или умение даёт знать без выбора: «Избранный враг»
     * следопыта даёт «Метку охотника». Всегда подготовлены и лимит подготовки не тратят.
     */
    @Schema(description = "Заклинания, которые выдаются без выбора",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private SpellGrant spells;

    /**
     * Заклинания, которые умение добавляет в список класса: заклинания домена жреца,
     * клятвы паладина, покровителя колдуна. Игрок их не знает готовыми — он МОЖЕТ их
     * подготовить наравне с классовыми, поэтому это не {@link #spells}.
     */
    @Schema(description = "Заклинания, добавляемые в список класса",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private SpellListExpansion spellList;

    /**
     * Ресурсы со своим счётчиком на листе: очки чародейства, кости превосходства, ярость.
     *
     * <p>Дублировать сюда ресурсы, у которых есть колонка таблицы прогрессии, не нужно:
     * колонка с заданным восстановлением ({@code ClassTableColumn.resourceRecovery}) — это
     * и есть счётчик, и выгрузка в VTTG собирает его оттуда. Блок нужен ресурсам без
     * колонки, у которых максимум задан формулой («Второе дыхание» — по бонусу мастерства).</p>
     */
    @Schema(description = "Ресурсы со своим счётчиком на листе",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ResourceCounter> counters;

    /**
     * Черты, которые умение выдаёт без выбора — ссылками на записи справочника.
     *
     * <p>Отдельно от {@link #choices}: выбор из одной черты всё равно спрашивал бы игрока,
     * а здесь спрашивать нечего — лист кладёт черту сам, как предыстория кладёт свою черту
     * происхождения.</p>
     */
    @Schema(description = "Черты, которые выдаются без выбора",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<EntityRef> feats;
}
