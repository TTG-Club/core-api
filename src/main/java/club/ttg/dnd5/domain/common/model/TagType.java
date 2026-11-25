package club.ttg.dnd5.domain.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public enum TagType {
    TAG_BOOK,
    TAG_FEATURE,
    TAG_SPECIES;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    @Setter
    public abstract static class BaseUrl {
        @Schema(description = "unique URL", requiredMode = Schema.RequiredMode.REQUIRED)
        private String url;
        @JsonProperty(value = "image")
        @Schema(description = "image URL")
        private String imageUrl;
        /**
         * Кадр обложки {@code image}. {@code null} в запросе стирает сохранённый кадр
         * (фронт шлёт его, когда кадр сброшен или картинка заменена).
         */
        @Schema(description = "Кадр обложки; null — не настроен")
        private ImageFrame imageFrame;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<String> gallery = new ArrayList<>();
    }
}
