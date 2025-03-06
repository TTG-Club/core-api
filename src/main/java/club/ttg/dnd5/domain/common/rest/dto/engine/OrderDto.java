package club.ttg.dnd5.domain.common.rest.dto.engine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class OrderDto {
	@Schema(description = "The name field for order", defaultValue = "name")
    private String field;
	@Schema(description = "The order direction: ASC or DESC", defaultValue = "asc")
    private String direction;

	public OrderDto(String pair) {
		List<String> strings = Arrays.stream(pair.split(" ")).toList();

		this.field = strings.get(0);
		this.direction = strings.get(1);
	}
}
