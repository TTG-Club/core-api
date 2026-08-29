package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.common.model.mechanics.ChoiceScaling;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

/**
 * Настройка выбора из списка вариантов умения: таинственные воззвания колдуна,
 * манёвры воина, метамагия чародея.
 *
 * <p>Само наличие блока и означает, что список выбираемый: без него варианты
 * остаются справкой на странице класса, как и были, и лист персонажа о них не
 * спрашивает. Пул выбора — {@link ClassFeature#getOptions()} того же умения, а
 * доступность варианта по уровню задаёт его собственный
 * {@link ClassFeatureOption#getRequiredClassLevel()}: второго списка вариантов
 * ради выбора автор не набирает.</p>
 *
 * <p>Количество растёт ступенями {@link #scaling} — тем же способом, что у
 * выбора в механике ({@link club.ttg.dnd5.domain.common.model.mechanics.MechanicChoice}):
 * ступень называет, сколько всего выбрано К её уровню, а не сколько добавилось.
 * Воззваний у колдуна одно с первого уровня и три со второго — значит, на
 * втором игрок выбирает два новых.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassFeatureOptionsChoice {

    @Schema(description = "Подпись выбора; пусто — название списка вариантов умения",
            example = "Таинственные воззвания")
    private String label;

    @Schema(description = "Сколько вариантов выбирают; пусто — один", example = "1")
    private Integer count;

    @Schema(description = "Ступени количества по уровням класса: с какого уровня сколько выбрано ВСЕГО")
    private List<ChoiceScaling> scaling;

    public ClassFeatureOptionsChoice(ClassFeatureOptionsChoice choice) {
        this.label = choice.getLabel();
        this.count = choice.getCount();
        this.scaling = Optional.ofNullable(choice.getScaling())
                .orElse(List.of())
                .stream()
                .map(step -> new ChoiceScaling(step.getLevel(), step.getCount()))
                .toList();
    }
}
