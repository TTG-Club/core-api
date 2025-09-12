package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.dto.base.SelectableEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@RequiredArgsConstructor
public enum CasterType implements SelectableEnum {
    FULL("Полноценный заклинатель"),
    HALF("1/2"),
    THIRD("1/3"),
    NONE("Не владеет заклинаниями"),
    PACT("Магия договора");

    @Getter
    private final String label;

    @Override
    public String getValue() {
        return name();
    }

}