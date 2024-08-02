package club.ttg.dnd5.model.character;

import jakarta.persistence.*;

@Entity
@Table(name = "class_features")
public class ClassFeature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
}
