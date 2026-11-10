package club.ttg.dnd5.domain.communitynews.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommunityNewsStatus {
    DRAFT("Черновик"),
    SCHEDULED("Запланирована"),
    ACTIVE("Активна"),
    INACTIVE("Неактивна");

    private final String name;
}
