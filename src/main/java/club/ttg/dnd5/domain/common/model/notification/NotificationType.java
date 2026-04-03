package club.ttg.dnd5.domain.common.model.notification;

import lombok.Getter;

@Getter
public enum NotificationType {
    PHRASE("Фраза"),
    NEWS("Новость"),
    ADVERTISING("Реклама");

    private final String name;

    NotificationType(String name) {
        this.name = name;
    }
}
