package club.ttg.dnd5.domain.user.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignInDto {
    private String usernameOrEmail;
    private String password;
    private boolean remember;
}
