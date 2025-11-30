package club.ttg.dnd5.domain.moderation.rest.dto;

import club.ttg.dnd5.domain.moderation.model.StatusType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ModerationShortResponse {
    private String url;

    @Enumerated(EnumType.STRING)
    private StatusType statusType;

    private String comment;

    private Instant updatedAt;
}
