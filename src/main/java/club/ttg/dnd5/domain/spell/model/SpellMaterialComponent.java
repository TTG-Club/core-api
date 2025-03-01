package club.ttg.dnd5.domain.spell.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SpellMaterialComponent {
    private String name; // Название компонента
    private Integer price; // Цена (используем Integer вместо int, чтобы допустить null)
    private ComparisonOperator comparison; // Оператор сравнения
    private boolean consumable; // Расходуемый или нет
}