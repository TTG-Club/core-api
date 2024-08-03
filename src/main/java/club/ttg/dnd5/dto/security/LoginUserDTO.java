package club.ttg.dnd5.dto.security;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class LoginUserDTO {
    private String email;
    private String password;
}