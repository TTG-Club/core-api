package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Coin {
    CC("медная монета", "мм", 1/100f),
    SC("серебрённая монета", "см", 1/10f),
    EC("Электрумовая монета", "эм", 1/2f),
    GC("золотая монета", "зм", 1f),
    PC("платиновая монета", "пм", 10f);

    private final String name;
    private final String shortName;
    private final float exchangeForGold;
}
