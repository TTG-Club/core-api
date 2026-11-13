package club.ttg.dnd5.domain.tool.dmscreen.rest.dto;

import club.ttg.dnd5.domain.tool.dmscreen.model.DmScreenWindow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DmScreenRequest {

    @Schema(description = "Все окна ширмы: заменяют сохранённые целиком; не больше 100")
    private List<DmScreenWindow> windows;
}
