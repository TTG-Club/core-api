package club.ttg.dnd5.domain.beastiary.model.sense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class Senses {
    @Schema(description = "Дополнительные чувства")
    private Collection<Sense> senses;

    @Schema(description = "Пассивная внимательность")
    private byte passivePerception;
}
