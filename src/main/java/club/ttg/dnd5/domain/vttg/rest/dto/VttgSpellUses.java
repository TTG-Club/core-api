package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Заряды использования заклинания в формате компендиума VTTG ({@code SpellUses}).
 *
 * <p>Для врождённой и расовой магии и заклинаний существ: ограниченное число
 * применений, не тратящее ячейки заклинателя. При {@code recovery = "atWill"}
 * заряды не расходуются.</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgSpellUses {
    private Integer max;
    /**
     * Текущее число зарядов. Справочник хранит только максимум — запись не
     * принадлежит конкретному персонажу, поэтому в компендиум уходит полный запас.
     */
    private Integer current;
    private String recovery;
}
