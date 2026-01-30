package club.ttg.dnd5.domain.user.rest.controller;

import club.ttg.dnd5.domain.user.rest.dto.UserDto;
import club.ttg.dnd5.domain.user.rest.dto.UserProfileShortResponse;
import club.ttg.dnd5.domain.user.service.UserService;
import club.ttg.dnd5.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return SecurityUtils.getUserDto();
    }

    @Operation(summary = "Получение профиля пользователя для бокового меню")
    @GetMapping("/profile/short")
    public UserProfileShortResponse getUserProfileShort() {
        return userService.getUserProfileShort();
    }

    @Operation(summary = "Получение списка ролей")
    @GetMapping("/roles")
    public List<String> getRoles() {
        UserDto userDto = SecurityUtils.getUserDto();
        return userDto.getRoles();
    }
}
