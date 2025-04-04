package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

/**
 * Существо из бестиария
 */
@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "bestiary",
        indexes = {
                @Index(name = "url_index", columnList = "url"),
                @Index(name = "name_index", columnList = "name, english, alternative")
        }
)
public class Beast extends NamedEntity {
    /**
     * Размеры существа.
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "beast_id")
    private Collection<BeastSize> sizes;

    /**
     * Типы существа.
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "beast_id")
    private Collection<BeastCategory> categories;

    @Enumerated(EnumType.STRING)
    private Alignment alignment;

    /**
     * Класс доспеха
     */
    private byte armorClass;
    /**
     * Дополнительное описание класса доспеха (для призванных существ)
     */
    private String armorClassText;

    /**
     * Количество хит дайсов
     */
    @Column(name ="hit_deces")
    private Short countHitDice;
    /**
     * Описание хитов если хит дайсы отсутствуют (например у призванных существ или созданных заклинанием)
     */
    @Column(name ="hit")
    private String hitText;

    /**
     * Характеристики существа
     */
    @OneToMany(cascade = {CascadeType.ALL})
    private Collection<BeastAbility> abilities;

    /**
     * Особенности существа
     */
    @OneToMany(cascade = CascadeType.ALL)
    private Collection<BeastTrait> traits;
}
