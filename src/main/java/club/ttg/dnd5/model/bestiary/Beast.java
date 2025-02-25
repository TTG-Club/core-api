package club.ttg.dnd5.model.bestiary;

import club.ttg.dnd5.dictionary.Alignment;
import club.ttg.dnd5.model.base.HasSourceEntity;
import club.ttg.dnd5.model.base.NamedEntity;
import club.ttg.dnd5.model.book.Source;
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
@Table(name = "bestiary")
public class Beast extends NamedEntity implements HasSourceEntity {
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
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "beast_id")
    private Collection<BeastAbility> abilities;

    /**
     * Особенности существа
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "beast_id")
    private Collection<BeastTrait> traits;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "source")
    private Source source = new Source();
}
