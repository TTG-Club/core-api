package club.ttg.dnd5.domain.communitynews.rest.dto;

import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Полная новость сообщества: краткая карточка + текст и даты. */
@NoArgsConstructor
@Getter
@Setter
public class CommunityNewsDetailedResponse extends CommunityNewsShortResponse {

    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    @Schema(description = "Полный текст (разметка); null — новость из одного превью")
    private String content;

    @NotNull
    @Schema(description = "Дата добавления")
    private String createdAt;

    @NotNull
    @Schema(description = "Дата обновления")
    private String updatedAt;
}
