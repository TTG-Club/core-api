package club.ttg.dnd5.dto.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class KeyValueDto {
    private String key;
    private Object value;
}
