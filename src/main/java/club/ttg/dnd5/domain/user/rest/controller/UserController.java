package club.ttg.dnd5.domain.user.rest.controller;

import club.ttg.dnd5.domain.user.model.Role;
import club.ttg.dnd5.domain.user.repository.RoleRepository;
import club.ttg.dnd5.domain.user.rest.dto.UserDto;
import club.ttg.dnd5.domain.user.rest.dto.UserProfileShortResponse;
import club.ttg.dnd5.domain.user.service.UserService;
import club.ttg.dnd5.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Tag(name = "Пользователи")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;
    private final RoleRepository roleRepository;

    @Secured("ADMIN")
    @Operation(summary = "Получение пользователей")
    @GetMapping
    public Collection<UserDto> getUsers(
            @RequestParam(required = false) String search
    )
    {
        return userService.getUsers(search);
    }

    @Secured("ADMIN")
    @Operation(summary = "Получение списка всех ролей")
    @GetMapping("/roles")
    public Collection<String> getUserRoles()
    {
        return roleRepository.findAll().stream().map(Role::getName).toList();
    }

    @Secured("USER")
    @Operation(summary = "Получение профиля пользователя")
    @GetMapping("/profile")
    public UserDto getUser()
    {
        return SecurityUtils.getUserDto();
    }

    @Secured("USER")
    @Operation(summary = "Получение профиля пользователя для бокового меню")
    @GetMapping("/profile/short")
    public UserProfileShortResponse getUserProfileShort()
    {
        return userService.getUserProfileShort();
    }

    @Secured("USER")
    @Operation(summary = "Получение списка ролей текущего пользователя")
    @GetMapping("/roles")
    public List<String> getRoles()
    {
        UserDto userDto = SecurityUtils.getUserDto();
        return userDto.getRoles();
    }

    @Secured("ADMIN")
    @Operation(summary = "Изменение ролей пользователя")
    @PutMapping("/{userId}/roles")
    public UserDto updateUserRoles(
            @PathVariable UUID userId,
            @RequestBody List<String> roles
    )
    {
        return userService.updateUserRoles(userId, Set.copyOf(roles));
    }
}