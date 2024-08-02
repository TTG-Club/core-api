package club.ttg.dnd5.model;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Name {
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String english;
    private String alternative;
}
