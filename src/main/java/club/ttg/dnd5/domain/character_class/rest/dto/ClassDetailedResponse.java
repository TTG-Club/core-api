package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumn;
import club.ttg.dnd5.domain.character_class.model.mechanics.ClassMechanics;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import club.ttg.dnd5.domain.common.rest.dto.EquipmentOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.FeatSpellListGroupResponse;
import club.ttg.dnd5.domain.common.rest.dto.GrantedSpellResponse;
import club.ttg.dnd5.domain.common.rest.dto.select.DiceOptionDto;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassDetailedResponse extends BaseResponse {

    @Schema(description = "Кость хитов класса с опциями")
    private DiceOptionDto hitDice;

    @Schema(description = "Основные характеристики")
    private String primaryCharacteristics;

    @Schema(description = "Владения класса")
    private ClassProficiencyDto proficiency;

    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    @Schema(description = "Снаряжение класса в формате Markdown")
    private String equipment;

    @Schema(description = "Стартовое снаряжение вариантами выбора")
    private List<EquipmentOptionDto> startingEquipment;

    @Schema(description = "Спасброски класса")
    private String savingThrows;

    /**
     * Те же спасброски характеристиками словаря: строка выше — для показа, эта
     * коллекция — для потребителей, которые владение применяют.
     */
    @Schema(description = "Спасброски класса характеристиками словаря")
    private Collection<Ability> savingThrowAbilities;

    @Schema(description = "Список особенностей класса")
    private List<ClassFeatureDto> features;

    @Schema(description = "Таблица прогрессии класса")
    private List<ClassTableColumn> table;

    @Schema(description = "Тип заклинателя", example = "FULL")
    private CasterType casterType;

    @Schema(description = "Характеристика, которой класс колдует")
    private Ability spellcastingAbility;

    @Schema(description = "Уровень класса, с которого работает заклинательство", example = "3")
    private Integer spellcastingStartLevel;

    @Schema(description = "Подпись группы подклассов", example = "Воинский архетип")
    private String subclassLabel;

    @Schema(description = "Уровень, на котором выбирается подкласс", example = "3")
    private Integer subclassLevel;

    @Schema(description = "Механика влияния класса на лист персонажа")
    private ClassMechanics mechanics;

    /**
     * Заклинания, которые даёт сама запись класса, — записями справочника.
     *
     * <p>Той же формой, что у умения ({@code ClassFeatureDto.grantedSpells}): в механике
     * лежат ссылки, а листу персонажа нужен круг заклинания. Заклинания умений сюда не
     * попадают — они лежат у самих умений.</p>
     */
    @Schema(description = "Заклинания, выдаваемые самим классом, с данными справочника")
    private List<GrantedSpellResponse> grantedSpells;

    /**
     * Расширение списка заклинаний самой записью класса — списками с данными справочника.
     * Не выдача: персонаж эти заклинания лишь может выучить или подготовить.
     */
    @Schema(description = "Расширение списка заклинаний классом с данными справочника")
    private Collection<FeatSpellListGroupResponse> spellListGroups;

    @Schema(description = "Активные эффекты класса в вокабуляре VTTG")
    private List<ActiveEffect> activeEffects;

    @Schema(description = "Есть ли подклассы")
    private boolean hasSubclasses;

    @Schema(description = "Ссылка на родительский класс")
    private ClassShortResponse parent;
}
