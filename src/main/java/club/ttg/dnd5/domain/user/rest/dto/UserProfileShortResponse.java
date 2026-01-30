package club.ttg.dnd5.domain.user.rest.dto;

import club.ttg.dnd5.dto.base.KeyValueDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class UserProfileShortResponse {

    private String username;
    private String email;
    private String image;
    private List<KeyValueDto> statistics;
}
