package club.ttg.dnd5.domain.moderation.rest.dto;

import club.ttg.dnd5.domain.moderation.model.StatusType;
import club.ttg.dnd5.domain.common.model.SectionType;
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
public class ModerationResponse {
    private String url;

    @Enumerated(EnumType.STRING)
    private SectionType sectionType;

    @Enumerated(EnumType.STRING)
    private StatusType statusType;

    private String comment;

    private Instant createdAt;

    private Instant updatedAt;

    private String username;
}
