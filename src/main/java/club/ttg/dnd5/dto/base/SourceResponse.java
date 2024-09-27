package club.ttg.dnd5.dto.base;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SourceResponse implements HasSourceDTO {
    private Short page;
    private String source;
}