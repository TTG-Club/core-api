package club.ttg.dnd5.domain.user.rest.dto;

import club.ttg.dnd5.dto.base.TimestampDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto extends TimestampDto {
    private String username;
    private String email;
    private List<String> roles;
}
