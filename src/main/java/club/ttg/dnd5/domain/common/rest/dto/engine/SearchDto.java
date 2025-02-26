package club.ttg.dnd5.domain.common.rest.dto.engine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchDto {
	@Schema(description = "строка поиска", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private String value;
	@Schema(description = "true если нужно точное совпадение", example = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private Boolean exact;
}
