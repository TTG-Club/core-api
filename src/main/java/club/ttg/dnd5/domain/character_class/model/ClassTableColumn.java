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

    @Schema(description = """
            Стабильный ключ колонки. Пусто — ключ выводится из подписи, как было до его
            появления; заполняют его, когда подпись переводят или меняют, а потраченный
            остаток ресурса на уже сохранённых листах терять нельзя.""")
    private String key;

    @Schema(description = "Краткая подпись для компактной плитки ресурса; пусто — берётся имя колонки")
    private String shortName;

    /**
     * Когда восстанавливается ресурс колонки — прежний способ записать классовый ресурс.
     *
     * <p>Редактор такие колонки больше не заводит: ресурс класса описывается счётчиком
     * механики ({@code ClassMechanics.counters} у самого класса либо у его умения), где у
     * него есть и формула максимума, и нижняя граница, и порция короткого отдыха. Поле
     * читается ради классов, которые ещё не переписаны: у них ресурс живёт колонкой.</p>
     */
    @Schema(description = "Когда восстанавливается ресурс колонки; прежний способ записи — "
            + "новые ресурсы описываются счётчиком механики")
    private ClassResourceRecovery resourceRecovery = ClassResourceRecovery.NONE;

    @Schema(description = "Что колонка означает для мастера повышения уровня")
    private ClassTableColumnPurpose purpose = ClassTableColumnPurpose.NONE;

    List<ClassTableItem> scaling;

    public ClassTableColumn(String name, List<ClassTableItem> scaling) {
        this(name, null, null, ClassResourceRecovery.NONE, ClassTableColumnPurpose.NONE, scaling);
    }
}
