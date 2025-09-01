package club.ttg.dnd5.domain.character_class.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassTableColumn {
    private String name;
    List<ClassTableItem> scaling;
}
