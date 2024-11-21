package club.ttg.dnd5.dto.character;

import club.ttg.dnd5.dto.base.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


@JsonInclude(JsonInclude.Include.NON_NULL)

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
@Builder
@Schema(description = "Информация о происхождении")
public class BackgroundDto extends BaseDTO {
}
