package club.ttg.dnd5.dto.base;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SourceResponse implements HasSourceDTO {
    private String source;
    private Short page;
}