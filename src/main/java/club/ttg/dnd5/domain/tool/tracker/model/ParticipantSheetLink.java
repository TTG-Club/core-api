package club.ttg.dnd5.domain.tool.tracker.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Привязка участника-игрока к листу персонажа: по ней трекер показывает аватар, открывает сам
 * лист и пишет в него текущие хиты. Хранится в jsonb-колонке участника — набор полей задаёт
 * фронтенд, сервер ими не распоряжается.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ParticipantSheetLink {

    @NotNull
    @Schema(description = "Идентификатор листа персонажа")
    private UUID sheetId;

    @NotNull
    @Size(max = 10)
    @Schema(description = "Откуда лист: own — свой, saved — сохранённый чужой по ссылке")
    private String source;

    @Nullable
    @Schema(description = "Токен ссылки «поделиться». NULL — свой лист")
    private UUID shareToken;

    @Nullable
    @Schema(description = "Идентификатор сохранённой записи чужого листа — по нему пишутся его хиты")
    private UUID savedId;

    @Nullable
    @Size(max = 512)
    @Schema(description = "Аватар персонажа. NULL — не загружен")
    private String avatarUrl;
}
