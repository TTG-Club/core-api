package club.ttg.dnd5.domain.common.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MulticlassRequest {
    @JsonProperty("class")
    @Schema(description = "Url базового класса (legacy формат)")
    private String url;
    @Schema(description = "URL базового подкласса (legacy формат)")
    private String subclass;
    @Schema(description = "Уровень базового класса (legacy формат)")
    private Integer level;
    @Schema(description = "Мультиклассы (legacy формат)")
    private List<MulticlassDto> classes;

    @Schema(description = "Упорядоченная последовательность сегментов уровней. " +
            "Каждый элемент — это блок уровней, взятых подряд в одном классе. " +
            "Один и тот же класс может встречаться несколько раз. " +
            "Первый элемент считается основным классом (определяет спасброски, владения и т.д.). " +
            "Поле level — абсолютный (накопленный) уровень класса после этого сегмента. " +
            "Пример: [{class: 'fighter', level: 3}, {class: 'wizard', level: 2}, {class: 'fighter', level: 4}] " +
            "означает: воин 1-3, волшебник 1-2, воин 4 (ещё 1 уровень воина).")
    private List<MulticlassLevelEntry> levels;
}
