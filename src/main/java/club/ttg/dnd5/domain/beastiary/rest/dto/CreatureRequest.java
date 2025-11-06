package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.beastiary.model.CreatureAbilities;
import club.ttg.dnd5.domain.beastiary.model.CreatureCategory;
import club.ttg.dnd5.domain.beastiary.model.CreatureHit;
import club.ttg.dnd5.domain.beastiary.model.CreatureInitiative;
import club.ttg.dnd5.domain.beastiary.model.language.CreatureLanguages;
import club.ttg.dnd5.domain.beastiary.model.CreatureSize;
import club.ttg.dnd5.domain.beastiary.model.CreatureSkill;
import club.ttg.dnd5.domain.beastiary.model.CreatureSpeeds;
import club.ttg.dnd5.domain.beastiary.model.sense.Senses;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
    @Schema(description = "Уязвимости, Сопротивления, Иммунитеты")
    private CreatureDefenses defenses;
    @Schema(description = "Снаряжение", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String equipments;
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
    private LegendaryActionRequest legendary;
    @Schema(description = "Описание логова")
    private CreatureLairRequest lair;
    @Schema(description = "Секция описательных характеристик существа")
    private CreatureSectionRequest section;
}
