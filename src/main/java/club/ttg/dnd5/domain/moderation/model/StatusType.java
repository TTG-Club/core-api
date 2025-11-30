package club.ttg.dnd5.domain.moderation.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum StatusType {

    DRAFT("Черновик"),
    REVIEW ("На проверке"),
    REJECTED ("Отклонено"),
    PUBLIC("Видно всем"),
    PRIVATE("Видно по ссылке"),
    HIDDEN("Скрыто");

    @Getter
    @JsonValue
    private final String value;
}
