package club.ttg.dnd5.domain.charlist.model;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.RangeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharAction {
    private String name;
    private String snippet;
    private String description;

    private RangeType rangeType;

    private Ability ability;
    private Byte diceCount;
    private Dice dice;
    private Short fixedValue;
    private DamageType damageType;
    private Ability saveType;
    private Byte fixedSaveDc;
    private Byte range;
    private Byte longRange;
    private AreaOfEffect aoe;
    private Activation activation;
}
