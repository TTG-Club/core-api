package club.ttg.dnd5.dto.base.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FilterDto {
    List<AbstractFilteringCollection<?>> filters;
}
