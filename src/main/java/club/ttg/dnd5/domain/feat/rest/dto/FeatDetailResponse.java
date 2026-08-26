package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.feat.model.prerequisite.FeatPrerequisite;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FeatDetailResponse extends BaseResponse {
    @Schema(description = "Категория", examples = {"черта происхождения", "общая черта"})
    private String category;

    /**
     * Требование строкой, как оно напечатано в книге. Отдаётся как есть и после появления
     * разобранного {@link #prerequisiteDetails}: разобрать удаётся не всё, а показать
     * требование нужно всегда — и у черт, разбора которых ещё нет, это единственная запись.
     */
    @Schema(description = "Предварительное условие", examples = {"черта происхождения", "общая черта"})
    private String prerequisite;

    /** Требование в разобранном виде — по нему визард выбора черты фильтрует список. */
    @Schema(description = "Предварительное условие в разобранном виде")
    private FeatPrerequisite prerequisiteDetails;

    /**
     * Механика влияния черты на лист персонажа — доменной моделью как есть.
     *
     * <p>Та же форма, что принимает {@link FeatRequest#getMechanics()}: сайт показывает и
     * шлёт одно и то же, ничего не пересобирая по дороге. Почему модель, а не свои DTO со
     * слагами — см. javadoc {@link FeatRequest}.</p>
     */
    @Schema(description = "Механика влияния черты на лист персонажа")
    private FeatMechanics mechanics;

    /**
     * Активные эффекты черты в вокабуляре VTTG.
     *
     * <p>Отдаются вместе с деталью, а не только в «сыром» ответе мастерской: их считает
     * лист персонажа сайта — так же, как эффекты магического предмета. Пустой список в
     * ответ не пишется.</p>
     */
    @Schema(description = "Активные эффекты черты")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ActiveEffect> activeEffects;
    @Schema(description = "Повторяемость")
    private Boolean repeatability;
    @Schema(description = "Предыстории, дающие эту черту")
    private Collection<FeatBackgroundDto> backgrounds;

    /**
     * Заклинания из {@code mechanics.spells}, дополненные данными справочника.
     *
     * <p>В механике они лежат ссылками — круг и школа берутся из самой записи заклинания
     * и в снимке разошлись бы с каталогом. Но потребителю их знать нужно: лист персонажа
     * без круга не положит заклинание в книгу. Поэтому деталь отдаёт их отдельным полем,
     * дополненными, — ровно как вид отдаёт врождённые заклинания.</p>
     *
     * <p>Вместе с записью справочника едет и {@code requiredLevel} — уровень персонажа, с
     * которого заклинание доступно: у метки дракона «Малое восстановление» приходит на
     * третьем уровне, и без уровня страница показала бы его наравне с первым.</p>
     */
    @Schema(description = "Выдаваемые чертой заклинания с данными справочника")
    private Collection<FeatGrantedSpellResponse> grantedSpells;

    /**
     * Заклинания из {@code mechanics.spellList}, дополненные данными справочника —
     * таблица «Заклинания метки».
     *
     * <p>Отдельным полем от {@link #grantedSpells}, потому что это другая механика: их
     * не выдают, а лишь добавляют в список класса. Круг для группировки таблицы берётся
     * из самой записи заклинания и потому приходит только здесь, а не в механике.</p>
     *
     * <p>Все заклинания блока подряд, из всех списков сразу. Кому важно, с какого уровня
     * список открывается и сколько заклинаний из него берут, — читает
     * {@link #spellListGroups}.</p>
     */
    @Schema(description = "Заклинания, добавляемые в список заклинаний, с данными справочника")
    private Collection<SpellShortResponse> spellListSpells;

    /**
     * Те же заклинания, но разложенные по спискам — с уровнем доступа и количеством.
     *
     * <p>Отдельным полем, а не заменой {@link #spellListSpells}: тому, кто просто рисует
     * таблицу по кругам, разбивка не нужна, а листу персонажа без неё не понять, что на
     * первом уровне открыт лишь первый список.</p>
     */
    @Schema(description = "Списки заклинаний по уровням доступа с данными справочника")
    private Collection<FeatSpellListGroupResponse> spellListGroups;
}
