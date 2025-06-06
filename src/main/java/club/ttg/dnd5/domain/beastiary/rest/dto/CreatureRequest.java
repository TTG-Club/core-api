package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.beastiary.model.CreatureAbilities;
import club.ttg.dnd5.domain.beastiary.model.CreatureCategory;
import club.ttg.dnd5.domain.beastiary.model.CreatureEquipment;
import club.ttg.dnd5.domain.beastiary.model.CreatureHit;
import club.ttg.dnd5.domain.beastiary.model.CreatureInitiative;
import club.ttg.dnd5.domain.beastiary.model.language.CreatureLanguages;
import club.ttg.dnd5.domain.beastiary.model.CreatureSize;
import club.ttg.dnd5.domain.beastiary.model.CreatureSkill;
import club.ttg.dnd5.domain.beastiary.model.CreatureSpeeds;
import club.ttg.dnd5.domain.beastiary.model.sense.Senses;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CreatureRequest extends BaseRequest {
    @Schema(description = "Размеры")
    private CreatureSize sizes;
    @Schema(description = "Типы")
    private CreatureCategory types;
    @Schema(description = "Мирровозрение")
    private Alignment alignment;
    @Schema(description = "Класс доспеха")
    private ArmorRequest ac;
    @Schema(description = "Бонус инициативы")
    private CreatureInitiative initiative;
    @Schema(description = "Хиты")
    private CreatureHit hit;
    @Schema(description = "Скорости")
    private CreatureSpeeds speeds;
    @Schema(description = "Характеристики")
    private CreatureAbilities abilities;
    @Schema(description = "Навыки")
    private Collection<CreatureSkill> skills;

    @Schema(description = "Уязвимости")
    private Collection<DamageType> vulnerabilities;
    @Schema(description = "Сопротивления")
    private Collection<DamageType> resistance;
    @Schema(description = "Иммунитеты к типам урона")
    private Collection<DamageType> immunityToDamage;
    @Schema(description = "Иммунитеты к состояниям")
    private Collection<Condition> immunityToCondition;
    @Schema(description = "Снаряжение")
    private Collection<CreatureEquipment> equipments;
    @Schema(description = "Чувства")
    private Senses senses;
    @Schema(description = "Языки")
    private CreatureLanguages languages;
    private Experience experience;
    @Schema(description = "Особенности")
    private Collection<TraitRequest> traits;
    @Schema(description = "Действия")
    private Collection<ActionRequest> actions;
    @Schema(description = "Бонусные действия")
    private Collection<ActionRequest> bonusActions;
    @Schema(description = "Реакции")
    private Collection<ActionRequest> reactions;
    @Schema(description = "Легендарные действия")
    private Collection<ActionRequest> legendaryActions;
    @Schema(description = "Секция описательных характеристик существа")
    private CreatureSectionRequest section;
}
