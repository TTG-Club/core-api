package club.ttg.dnd5.domain.common.model.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    PHRASE("Фраза"),
    NEWS("Новость"),
    ADVERTISING("Реклама");

    private final String name;
}
