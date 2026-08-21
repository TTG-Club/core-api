package club.ttg.dnd5.domain.background.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Информация о предыстории кратко")
@Getter
@Setter
public class BackgroundShortResponse extends ShortResponse {
    @Schema(description = "Характеристики:")
    private String abilityScores;

    /**
     * Черта предыстории названием и ссылкой.
     *
     * <p>Списку предысторий этого достаточно: и на сайте, и в мастере листа персонажа
     * черта — это то, чем предыстории отличаются друг от друга, а за ней самой ходить
     * в деталь каждой строки списка нельзя.</p>
     */
    @Schema(description = "Название черты, которую даёт предыстория", examples = "Посвящённый в магию")
    private String featName;

    @Schema(description = "Ссылка на черту, которую даёт предыстория", examples = "magic-initiate")
    private String featUrl;
}
