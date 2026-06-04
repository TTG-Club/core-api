package club.ttg.dnd5.domain.charlist.rest.dto;

import club.ttg.dnd5.domain.charlist.model.CharlistVisibility;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CharlistResponse {
    private UUID id;
    private String characterName;
    private Integer characterLevel;
    private String characterClass;
    private String data;
    private CharlistVisibility visibility;
    private String shareToken;
    private Instant createdAt;
    private Instant updatedAt;
}
