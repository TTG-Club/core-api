package club.ttg.dnd5.domain.charlist.rest.dto;

import club.ttg.dnd5.domain.charlist.model.CharlistVisibility;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Краткая информация о чарлисте (без полного JSON data).
 */
@Getter
@Setter
@Builder
public class CharlistShortResponse {
    private UUID id;
    private String characterName;
    private Integer characterLevel;
    private String characterClass;
    private CharlistVisibility visibility;
    private Instant createdAt;
    private Instant updatedAt;
}
