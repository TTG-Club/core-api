package club.ttg.dnd5.domain.article.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArticleStatus {
    DRAFT("Черновик"),
    SCHEDULED("Запланирована"),
    ACTIVE("Активна"),
    INACTIVE("Неактивна");

    private final String name;
}
