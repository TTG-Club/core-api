package club.ttg.dnd5.domain.character_class.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class ClassTableColumn {
    @Schema(description = "Имя колонки")
    private String name;

    @Schema(description = "Является ли значение колонки расходуемым ресурсом класса")
    private boolean resource;

    List<ClassTableItem> scaling;

    public ClassTableColumn(String name, List<ClassTableItem> scaling) {
        this(name, false, scaling);
    }
}
