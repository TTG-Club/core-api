package club.ttg.dnd5.domain.beastiary.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "beast_trait")
public class BeastTrait {
    @Id
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String english;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
}