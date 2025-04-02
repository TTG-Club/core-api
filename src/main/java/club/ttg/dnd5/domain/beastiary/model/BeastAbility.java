package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeastAbility {
  @Id
  private Long id;
  /**
   * Тип характеристики
   */
  @Enumerated(EnumType.STRING)
  private Ability ability;
  /**
   * Значение характеристики
   */
  private short value;
  /**
   * Если истина, то для этого навыка при спасбросках добавляется бонус мастерства
   */
  private boolean save;

  /**
   * Получение модификатора характеристики
   * @return модификатор характеристики
   */
  public byte getMod() {
    return (byte) ((value - 10) < 0 ? (value - 11) / 2 : (value - 10) / 2);
  }
}