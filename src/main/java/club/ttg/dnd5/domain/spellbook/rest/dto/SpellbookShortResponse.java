package club.ttg.dnd5.domain.spellbook.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpellbookShortResponse {

    @NotNull
    @Schema(description = "Идентификатор книги")
    private UUID id;

    @NotNull
    @Schema(description = "Название книги")
    private String name;

    @NotNull
    @Schema(description = "Логин владельца книги")
    private String ownerUsername;

    @Nullable
    @Schema(description = "Ключ ссылки для шаринга: отдаётся только владельцу книги")
    private UUID shareKey;

    @Schema(description = "Всего заклинаний в книге")
    private long spellCount;

    @Schema(description = "Из них подготовленных")
    private long preparedCount;

    @Nullable
    @Schema(description = "Дата создания")
    private Instant createdAt;

    @Nullable
    @Schema(description = "Дата последнего изменения")
    private Instant updatedAt;
}
