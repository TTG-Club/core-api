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
        private FilterGroupType type;           // FILTER or SINGLETON
        private SupportsConfig supports;        // { mode, union }
        private List<FilterValueMeta> values;   // for filter
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterValueMeta {
        private String id;      // SHA-256 short hash, enum name, or number
        private Object value;   // Full value for display
        private String name;    // Human readable name
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceGroupMeta {
        private String key;     // e.g. "official"
        private String name;    // e.g. "Официальные"
        private List<FilterValueMeta> values;
    }
}
