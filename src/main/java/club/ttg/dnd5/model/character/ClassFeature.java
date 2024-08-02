package club.ttg.dnd5.model.character;

import club.ttg.dnd5.model.Name;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "class_features")
public class ClassFeature extends Name {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private short level;
    @Column(columnDefinition = "TEXT")
    private String description;
}
