package club.ttg.dnd5.domain.tool.dmscreen.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Окно ширмы Мастера: открытая запись сайта либо заметка. Хранится элементом jsonb-массива и в том
 * же виде уходит клиенту, поэтому отдельного DTO нет. Поля — обёртки, а не примитивы: пропущенное
 * в запросе поле должно дать 400, а не молча стать нулём.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DmScreenWindow {

    @Schema(description = "Идентификатор окна, выдаёт клиент; до 64 символов")
    private String id;

    @Schema(description = "Что открыто в окне: путь записи сайта (с «/») либо «note:…» у заметки; до 256 символов")
    private String path;

    @Schema(description = "Заголовок окна, может быть пустым; до 256 символов")
    private String title;

    @Schema(description = "Положение левого края")
    private Double x;

    @Schema(description = "Положение верхнего края")
    private Double y;

    @Schema(description = "Ширина, больше нуля")
    private Double width;

    @Schema(description = "Высота, больше нуля")
    private Double height;

    @Schema(description = "Слой: чем больше, тем выше окно лежит")
    private Integer layer;

    /**
     * Имя в JSON задано явно: по геттеру Lombok свойство называлось бы {@code minimized}.
     */
    @JsonProperty("isMinimized")
    @Schema(description = "Окно свёрнуто")
    private Boolean minimized;

    /**
     * Текст заметки — разметка сайта в хранимом виде. Сервер её не разбирает и не меняет.
     */
    @Nullable
    @Schema(description = "Текст заметки (только у окон с path на «note:»); до 20000 символов")
    private String content;
}
