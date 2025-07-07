package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RechargeType {
    D3("3-6"),
    D4("4-6"),
    D5("6"),
    D6("6"),
    SLR("после короткого или продолжительного отдыха"),
    LR("после продолжительного отдыха");

    private final String name;
}
