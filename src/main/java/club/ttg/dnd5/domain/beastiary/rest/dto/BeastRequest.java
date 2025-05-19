package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.beastiary.model.BeastAbilities;
import club.ttg.dnd5.domain.beastiary.model.BeastEquipment;
import club.ttg.dnd5.domain.beastiary.model.BeastHit;
import club.ttg.dnd5.domain.beastiary.model.BeastTrait;
import club.ttg.dnd5.domain.beastiary.model.action.BeastAction;
import club.ttg.dnd5.domain.beastiary.model.language.BeastLanguages;
import club.ttg.dnd5.domain.beastiary.model.BeastSize;
import club.ttg.dnd5.domain.beastiary.model.BeastSkill;
import club.ttg.dnd5.domain.beastiary.model.BeastSpeeds;
import club.ttg.dnd5.domain.beastiary.model.BeastType;
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
public class BeastRequest extends BaseRequest {
    @Schema(description = "Размеры")
    private Collection<BeastSize> size;
    @Schema(description = "Типы")
    private Collection<BeastType> type;
    @Schema(description = "Мирровозрение")
    private Alignment alignment;
    @Schema(description = "Класс доспеха")
    private byte ac;
    @Schema(description = "Бонус инициативы", examples = "+0")
    private byte initiative;
    @Schema(description = "Хиты")
    private BeastHit hit;
    @Schema(description = "Скорости")
    private BeastSpeeds speed;
    @Schema(description = "Характеристики")
    private BeastAbilities abilities;
    @Schema(description = "Навыки")
    private Collection<BeastSkill> skills;

    @Schema(description = "Уязвимости")
    private Collection<DamageType> vulnerabilities;
    @Schema(description = "Сопротивления")
    private Collection<DamageType> resistance;
    @Schema(description = "Иммунитеты к типам урона")
    private Collection<DamageType> immunityToDamage;
    @Schema(description = "Иммунитеты к состояниям")
    private Collection<Condition> immunityToCondition;
    @Schema(description = "Снаряжение")
    private Collection<BeastEquipment> equipments;
    @Schema(description = "Языки")
    private BeastLanguages languages;
    @Schema(description = "Особенности")
    private Collection<BeastTrait> traits;
    @Schema(description = "Действия")
    private Collection<BeastAction> actions;
    @Schema(description = "Бонусные действия")
    private Collection<BeastAction> bonusActions;
    @Schema(description = "Реакции")
    private Collection<BeastAction> reactions;
    @Schema(description = "Легендарные действия")
    private Collection<BeastAction> legendaryActions;
}
