package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.character_class.model.mechanics.ClassMechanics;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassFeatureOptionRequest;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.rest.dto.Name;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import club.ttg.dnd5.util.SlugifyUtil;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class ClassFeatureOption {

    @Schema(description = "Stable option slug", example = "agonizing_blast")
    private String key;

    @Schema(description = "Option name")
    private Name name;

    @Schema(description = "Option description")
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String description;

    @Schema(description = "Оригинальное описание на английском языке (обычный текст)")
    private String original;

    @Schema(description = "Short additional label")
    private String additional;

    @Schema(description = "Option prerequisite")
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String prerequisite;

    @Schema(description = "Required class level for this option")
    private Integer requiredClassLevel;

    @Schema(description = "Hide option in subclass and multiclass contexts")
    private boolean hideInSubclasses;

    @Schema(description = "Вариант можно выбрать повторно на следующей ступени выбора")
    private boolean repeatable;

    /**
     * Механика влияния варианта на лист персонажа — той же моделью, что у самого умения.
     *
     * <p>Воззвание колдуна выдаёт заклинание, манёвр — владение приёмом, инфузия
     * изобретателя заводит ресурс: вариант делает с листом то же, что умение, и своя
     * модель для того же смысла означала бы второй разбор у каждого потребителя.</p>
     *
     * <p>Дары варианта действуют, только пока он выбран: невыбранный вариант — просто
     * строка справочника.</p>
     */
    @Schema(description = "Механика влияния варианта на лист персонажа")
    private ClassMechanics mechanics;

    @Schema(description = "Активные эффекты варианта в вокабуляре VTTG")
    private List<ActiveEffect> activeEffects;

    public ClassFeatureOption(ClassFeatureOptionRequest request) {
        this.key = StringUtils.hasText(request.getKey()) ? request.getKey() : buildKey(request.getName());
        this.name = request.getName();
        this.description = request.getDescription();
        this.original = request.getOriginal();
        this.additional = request.getAdditional();
        this.prerequisite = request.getPrerequisite();
        this.requiredClassLevel = request.getRequiredClassLevel();
        this.hideInSubclasses = request.isHideInSubclasses();
        this.repeatable = request.isRepeatable();
        this.mechanics = request.getMechanics();
        this.activeEffects = request.getActiveEffects();
    }

    public ClassFeatureOption(ClassFeatureOption option) {
        this.key = option.getKey();
        this.name = option.getName();
        this.description = option.getDescription();
        this.original = option.getOriginal();
        this.additional = option.getAdditional();
        this.prerequisite = option.getPrerequisite();
        this.requiredClassLevel = option.getRequiredClassLevel();
        this.hideInSubclasses = option.isHideInSubclasses();
        this.repeatable = option.isRepeatable();
        this.mechanics = option.getMechanics();
        this.activeEffects = option.getActiveEffects();
    }

    private String buildKey(Name name) {
        if (name == null) {
            return null;
        }

        String source = StringUtils.hasText(name.getEnglish()) ? name.getEnglish() : name.getName();
        return StringUtils.hasText(source) ? SlugifyUtil.getSlug(source).replace('-', '_') : null;
    }
}
