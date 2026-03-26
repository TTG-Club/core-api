package club.ttg.dnd5.domain.filter.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterMetadataResponse {
    private List<FilterGroupMeta> filters;
    private List<SourceGroupMeta> sources;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterGroupMeta {
        private String key;                     // e.g. "school"
        private String name;                    // e.g. "Школа магии"
        private String type;                    // e.g. "threeState" or "singleton"
        private List<FilterValueMeta> values;   // for threeState
        private Boolean state;                  // for singleton (true/false/null)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterValueMeta {
        private Object value;   // Enum name, id, etc.
        private String name;    // Human readable name
        private Boolean state;  // true=positive, false=negative, null=unchecked
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceGroupMeta {
        private String key;     // e.g. "official"
        private String name;    // e.g. "Официальные"
        private List<SourceValueMeta> values;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceValueMeta {
        private String value;   // Acronym e.g. "PHB"
        private String name;    // e.g. "Player's Handbook"
        private Boolean enabled; // true/false (or null meaning disabled)
    }
}
