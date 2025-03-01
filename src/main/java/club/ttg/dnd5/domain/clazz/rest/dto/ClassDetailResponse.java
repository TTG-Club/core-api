package club.ttg.dnd5.domain.clazz.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.rest.dto.BaseDto;
import club.ttg.dnd5.domain.common.GroupStrategy;
import club.ttg.dnd5.domain.common.rest.dto.NameDto;
import club.ttg.dnd5.domain.book.model.Source;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
@Schema(description = "Информация о классе или подклассе")
public class ClassDetailResponse extends BaseDto implements GroupStrategy {
    @Schema(description = "Основная характеристика")
    private String mainAbility;
    @Schema(description = "Хиты")
    private String hitDice;
    @Schema(description = "Владение и мастерство")
    private ClassMasteryDto mastery;

    @Schema(description = "Стартовое снаряжение")
    private String startEquipment;

    // Связанные сущности
    @Schema(description = "Url родительского класса если есть")
    private String parentUrl;
    @Schema(description = "URLs суб классов если есть")
    private Collection<String> subClassUrls;
    @Schema(description = "Умения класса")
    private Collection<ClassFeatureDto> features;

    private NameDto group = new NameDto();

    @Override
    public void determineGroup(final Source source) {
        if (group != null && source.getBookInfo() != null) {
            this.group.setName("Происхождение");
            this.group.setEnglish("Basic");
        }
    }
}
