package club.ttg.dnd5.model.bestiary;

import club.ttg.dnd5.dictionary.Size;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "beast_sizes")
public class BeastSize {
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private Size size;
    private String text;
}
