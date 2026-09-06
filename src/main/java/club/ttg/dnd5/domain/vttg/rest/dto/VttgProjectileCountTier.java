package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Порог уровня персонажа → полное число снарядов заговора
 * ({@code ProjectileCountTier} в компендиуме VTTG).
 *
 * <p>Число снарядов заговора растёт не от круга ячейки, а от уровня самого
 * заклинателя: с указанного уровня базовое число ЗАМЕНЯЕТСЯ этим.</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgProjectileCountTier {
    /** Минимальный уровень персонажа, с которого действует это число. */
    private Integer level;
    /** Полное число снарядов начиная с этого уровня. */
    private Integer count;
}
