package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheetEditorStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Запрос на редактирование или выданное право — для владельца листа. Логин и почта не отдаются:
 * владельцу хватает имени и аватарки, которые и так публичны в комментариях.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetEditorResponse {

    @NotNull
    @Schema(description = "Идентификатор записи: им владелец разрешает, отклоняет или отзывает право")
    private UUID id;

    @NotNull
    @Schema(description = "Отображаемое имя того, кто просит или редактирует")
    private String displayName;

    @Nullable
    @Schema(description = "Ссылка на аватарку; null — аватарки нет")
    private String avatarUrl;

    @NotNull
    @Schema(description = "PENDING — ждёт ответа, APPROVED — может редактировать")
    private CharacterSheetEditorStatus status;

    @Nullable
    @Schema(description = "Когда отправлен запрос")
    private Instant requestedAt;
}
