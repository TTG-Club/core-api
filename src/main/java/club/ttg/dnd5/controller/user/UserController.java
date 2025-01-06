package club.ttg.dnd5.controller.user;

import club.ttg.dnd5.dto.user.UserDto;
import club.ttg.dnd5.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Пользователи")

@Slf4j
@Hidden
@Secured("USER")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    @Operation(summary = "Получение профиля пользователя")
    @GetMapping("/profile")
    public UserDto getUser() {
        return SecurityUtils.getUserDto();
    }

    @Operation(summary = "Получение списка ролей")
    @GetMapping("/roles")
    public List<String> getRoles() {
        UserDto userDto = SecurityUtils.getUserDto();

        return userDto.getRoles();
    }

}
