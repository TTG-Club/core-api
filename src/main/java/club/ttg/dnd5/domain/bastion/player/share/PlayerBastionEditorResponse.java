package club.ttg.dnd5.domain.bastion.player.share;

import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionEditor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Запрос на правку бастиона или выданное право.
 *
 * @param id        запись; по ней владелец разрешает, отклоняет и отзывает
 * @param userId    кто просит
 * @param status    ждёт ответа или разрешено
 * @param createdAt когда попросил
 */
@Schema(description = "Редактор бастиона по ссылке")
public record PlayerBastionEditorResponse(UUID id, UUID userId, PlayerBastionEditor.Status status,
                                          Instant createdAt) {

    public static PlayerBastionEditorResponse of(PlayerBastionEditor editor) {
        return new PlayerBastionEditorResponse(editor.getId(), editor.getUserId(), editor.getStatus(),
                editor.getCreatedAt());
    }
}
