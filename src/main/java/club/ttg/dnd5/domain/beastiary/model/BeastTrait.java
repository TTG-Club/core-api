package club.ttg.dnd5.domain.beastiary.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeastTrait {
    private String name;

    private String english;

    private String description;
}