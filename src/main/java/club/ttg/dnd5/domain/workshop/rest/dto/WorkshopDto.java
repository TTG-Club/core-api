package club.ttg.dnd5.domain.workshop.rest.dto;

import club.ttg.dnd5.domain.common.model.SectionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WorkshopDto {
    private SectionType type;
    private Counters counters;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Counters {
        private Long created;
        private Long moderation;
        private Long draft;
    }
}
