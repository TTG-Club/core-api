package club.ttg.dnd5.domain.magic.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import club.ttg.dnd5.dto.base.filters.AbstractSearchRequest;
import club.ttg.dnd5.dto.base.filters.ThreeStateFilter;
import club.ttg.dnd5.dto.base.filters.ThreeStateSingleton;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO запроса фильтрации магических предметов.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MagicItemSearchRequest extends AbstractSearchRequest
{
    /** Категория магического предмета (3-state enum). */
    private ThreeStateFilter<MagicItemCategory> category;

    /** Редкость (3-state enum). */
    private ThreeStateFilter<Rarity> rarity;

    /** Настройка (3-state singleton). */
    private ThreeStateSingleton attunement;

    /** Есть заряды (3-state singleton). */
    private ThreeStateSingleton charges;

    /** Проклятие (3-state singleton). */
    private ThreeStateSingleton curse;
}
