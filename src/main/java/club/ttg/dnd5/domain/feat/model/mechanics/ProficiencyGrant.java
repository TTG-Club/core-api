package club.ttg.dnd5.domain.feat.model.mechanics;

import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.Language;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.EntityRef;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * Владения, которые черта выдаёт сразу и целиком: «Вы получаете владение воинским
 * оружием».
 *
 * <p>От {@link FeatChoice} отличается тем, что игрок ничего не выбирает. Выбор владения
 * («Умелый» — три навыка на выбор, «Мастер оружия» — один вид оружия) остаётся в
 * {@code mechanics.choices}: там у него есть и количество, и пул значений, и правила
 * вроде «только то, чем ещё не владеешь». Здесь ни того, ни другого не нужно —
 * достаточно перечислить выданное.</p>
 *
 * <p>Категории, а не отдельные виды: правила выдают владение группой («воинское
 * оружие», «средние доспехи»), и лист умеет хранить такую запись целиком, не
 * расписывая её по двадцати позициям.</p>
 *
 * <p>Владение с условием сюда не идёт — как и в {@link FeatModifiers}, условные
 * эффекты остаются в описании.</p>
 *
 * <p>Спасбросков здесь нет намеренно: спасбросками черты наделяют выбором («Устойчивый» —
 * характеристика на выбор), а это {@code choices}.</p>
 *
 * <p>Языки, наоборот, есть. Словарь {@link club.ttg.dnd5.domain.common.dictionary.Language}
 * и справочник языков листа расходятся и в названиях («дварфский» против «Дварфийский»,
 * «бездны» против «Абиссальный»), и в группировке (драконий здесь стандартный, на листе —
 * редкий), но расхождение сведено таблицей на стороне выгрузки, и лист принимает язык
 * своим названием.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProficiencyGrant {
    /**
     * Категории оружия. «Воинское оружие» — это пара
     * {@code MATERIAL_MELEE} и {@code MATERIAL_RANGED}: словарь делит категории ещё и по
     * дальнобойности, а правила — нет.
     */
    @Schema(description = "Категории оружия", examples = {"MATERIAL_MELEE", "MATERIAL_RANGED"})
    private Set<WeaponCategory> weaponCategories;

    @Schema(description = "Категории доспехов", examples = {"MEDIUM", "SHIELD"})
    private Set<ArmorCategory> armorCategories;

    /**
     * Навыки, которыми черта наделяет без выбора. Выбор навыков («Умелый» — три на
     * выбор) сюда не идёт: у него есть количество и пул, и живёт он в
     * {@code mechanics.choices}.
     */
    @Schema(description = "Навыки", examples = {"PERCEPTION", "STEALTH"})
    private Set<Skill> skills;

    /**
     * Инструменты. Ссылками на предметы справочника, а не словарём: инструменты живут в
     * разделе «Предметы», и лист хранит вместе с названием ссылку — чтобы открыть
     * описание прямо из панели владений.
     */
    @Schema(description = "Инструменты из справочника")
    private List<EntityRef> tools;

    /**
     * Языки, которые черта даёт знать без выбора («Знаток языков» — три языка на выбор,
     * и это уже {@code choices}, а вот «Дар Общения» даёт язык прямо).
     *
     * <p>Словарём, а не ссылками: языки — не записи справочника, и лист хранит их простым
     * списком названий.</p>
     */
    @Schema(description = "Языки", examples = {"DWARVISH", "ELVISH"})
    private Set<Language> languages;
}
