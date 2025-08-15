package club.ttg.dnd5.domain.beastiary.model.sense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Senses {
    @Schema(description = "Тёмное зрение")
    private Short darkvision;
    @Schema(description = "Тёмное зрение проникает через магическую тьму")
    private Boolean unimpeded;
    @Schema(description = "Истинное зрение")
    private Short truesight;
    @Schema(description = "Слепое зрение")
    private Short blindsight;
    @Schema(description = "Чувство вибрации")
    private Short tremorsense;

    @Schema(description = "Пассивная внимательность")
    private byte passivePerception;
}
