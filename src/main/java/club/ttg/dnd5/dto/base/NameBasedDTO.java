package club.ttg.dnd5.dto.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class NameBasedDTO implements HasNameResponse {
    private String name;
    private String english;
    private String alternative;
}
