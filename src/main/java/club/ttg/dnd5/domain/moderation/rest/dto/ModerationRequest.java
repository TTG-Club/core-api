package club.ttg.dnd5.domain.moderation.rest.dto;

import club.ttg.dnd5.domain.moderation.model.StatusType;
import club.ttg.dnd5.domain.common.model.SectionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ModerationRequest {
    private List<SectionType> sectionTypes;
    private List<StatusType> statusTypes;
    private String comment;
}
