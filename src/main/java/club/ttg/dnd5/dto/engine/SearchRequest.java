package club.ttg.dnd5.dto.engine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.Map;

@JsonInclude(Include.NON_NULL)

@Getter
@Setter
@NoArgsConstructor
public class SearchRequest {
	@Schema(description = "номер страницы", defaultValue = "0")
    public Integer page;
	@Schema(description = "размер страницы", defaultValue = "10")
    public Integer size = -1;
	@Schema(description = "поисковый запрос")
    public SearchDto search;
    @Schema(description = "фильтры")
    public Map<String, FilterDto> filters;
	@Schema(description = "сортировка")
    @JsonProperty("order")
    public Collection<OrderDto> orders;
}
