package club.ttg.dnd5.domain.magic.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Тематика магического предмета: к какой группе он относится по назначению.
 * Не зависит от категории — посох может быть и магией, и реликвией.
 */
@Getter
@AllArgsConstructor
public enum MagicItemTheme {
    ARMAMENT("вооружение"),
    MAGIC("магия"),
    RELIC("реликвии"),
    TOOL("инструменты");

    private final String name;
}
