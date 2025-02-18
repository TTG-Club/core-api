package club.ttg.dnd5.model.bestiary;

import club.ttg.dnd5.dictionary.Ability;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class BeastAbility {
    @Id
    private Long id;

    private Ability ability;
    private short value;
    private boolean save;
}
