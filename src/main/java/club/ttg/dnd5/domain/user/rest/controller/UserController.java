package club.ttg.dnd5.domain.user.rest.controller;

import club.ttg.dnd5.domain.user.model.UserPreferences;
import club.ttg.dnd5.domain.user.rest.dto.UserDto;
import club.ttg.dnd5.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Пользователи")

@Secured("USER")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Получение профиля пользователя")
    @GetMapping("/profile")
    public UserDto getUser() {
        return userService.getUserProfile();
    }

    @Operation(summary = "Получение списка ролей")
    @GetMapping("/roles")
    public List<String> getRoles() {
        UserDto userDto = userService.getUserProfile();
        return userDto.getRoles();
    }

    @Operation(summary = "Получение настроек пользователя")
    @GetMapping("/preferences")
    public UserPreferences getPreferences() {
        return userService.getPreferences();
    }

    @Operation(summary = "Обновление настроек пользователя")
    @RequestMapping(method = RequestMethod.PATCH, value = "/preferences")
    public UserPreferences updatePreferences(@RequestBody UserPreferences preferences) {
        return userService.updatePreferences(preferences);
    }
}
