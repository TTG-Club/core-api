package club.ttg.dnd5.domain.roadmap.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roadmap")
public class Roadmap {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String url;
    private String name;
    private String preview;
    private String description;
    private boolean visible;
}
