package club.ttg.dnd5.domain.magic.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Attunement {
    @Schema(description = "true если настройка требуется")
    private boolean requires;
    @Schema(description = "описание ограничения настройки (например только классом, видом или мировоззрением)")
    private String description;
}
