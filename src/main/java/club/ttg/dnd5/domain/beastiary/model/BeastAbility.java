package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeastAbility {
  /**
   * Тип характеристики
   */
  @Schema(description = "Название характеристики")
  private Ability ability;
  /**
   * Значение характеристики
   */
  @Schema(description = "Значение характеристики")
  private short value;

  /**
   * 0 - бонус мастерства не добавляется
   * 1 - добавляется бонус мастерства
   * 2 - добавляется удвоенный бонус мастерства
   */
  @Schema(description = "Множитель бонуса мастерства для спасброска")
  private short multiplier;

  /**
   * Получение модификатора характеристики
   * @return модификатор характеристики
   */
  public byte getMod() {
    return (byte) ((value - 10) < 0 ? (value - 11) / 2 : (value - 10) / 2);
  }
}