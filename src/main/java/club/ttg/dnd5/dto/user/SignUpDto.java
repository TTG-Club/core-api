package club.ttg.dnd5.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignUpDto {
    @NotBlank(message = "Необходимо ввести имя пользователя")
    @Min(value = 3, message = "Имя пользователя должно содержать не менее 3 символов")
    @Max(value = 100, message = "Слишком длинное имя пользователя")
    private String username;

    @NotBlank(message = "Необходимо ввести e-mail")
    @Email(message = "Некорректный e-mail")
    private String email;

    @NotBlank(message = "Необходимо ввести пароль")
    @Min(value = 8, message = "Пароль должен содержать не менее 8 символов")
    @Max(value = 128, message = "Слишком длинный пароль")
    private String password;
}
