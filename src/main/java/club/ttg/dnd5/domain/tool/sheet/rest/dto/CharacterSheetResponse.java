package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import com.fasterxml.jackson.databind.JsonNode;
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
public class CharacterSheetResponse {

    @NotNull
    @Schema(description = "Идентификатор листа")
    private UUID id;

    @NotNull
    @Schema(description = "Название листа")
    private String name;

    @Schema(description = "Лист удалён (виден только в истории при includeDeleted=true)")
    private boolean deleted;

    @Nullable
    @Schema(description = "Лист персонажа целиком (JSON фронтового формата); в списке у удалённых листов — null")
    private JsonNode data;

    @Nullable
    @Schema(description = "Токен ссылки «поделиться»; null — доступ по ссылке выключен. "
            + "Отдаётся только владельцу листа")
    private UUID shareToken;

    @Schema(description = "Лист открыт редактором, которому владелец дал право, а не самим владельцем: "
            + "удаление, ссылка и раздача прав ему недоступны")
    private boolean editor;

    @Schema(description = "Неотвеченные запросы на редактирование листа; заполняется только в списке "
            + "листов владельца")
    private int pendingEditRequests;

    @Schema(description = "Версия листа: её клиент присылает при сохранении, чтобы не затереть "
            + "правку, сделанную в другом месте")
    private long version;

    @Nullable
    @Schema(description = "Дата создания")
    private Instant createdAt;

    @Nullable
    @Schema(description = "Дата последнего изменения")
    private Instant updatedAt;
}
